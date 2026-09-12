package cn.iocoder.yudao.server.quote;

import com.fasterxml.jackson.databind.*;
import java.net.URI;
import java.net.http.*;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;
import static cn.iocoder.yudao.server.quote.QuoteTypes.*;

/** 参数建议器：读取 Skill 和后端资料。模型只返回允许调整的字段，不参与金额计算。 */
public final class QuoteSuggestionService {
    private final ObjectMapper mapper = new ObjectMapper();
    private final QuoteEngine engine;
    private final QuoteSkills skills;
    private final String url, model, key;
    public QuoteSuggestionService(QuoteEngine engine, QuoteSkills skills) {
        this(engine, skills, System.getenv("QUOTE_AI_CHAT_URL"), System.getenv("QUOTE_AI_MODEL"), System.getenv("QUOTE_AI_API_KEY"));
    }
    public QuoteSuggestionService(QuoteEngine engine, QuoteSkills skills, String url, String model, String key) {
        this.engine = engine; this.skills = skills; this.url = url; this.model = model; this.key = key;
    }
    private boolean present(String s) { return s != null && !s.isBlank(); }
    public String modelName() { return model == null ? "" : model; }
    public String mode() {
        if (!present(url) || !present(model) || !present(key)) return "config-error";
        return "model";
    }
    public Suggestion suggest(Request request) {
        Catalog data = engine.catalog();
        engine.validate(request, data);
        List<Skill> loaded = skills.load();
        BigDecimal after;
        String reason;
        String mode = mode();
        if (mode.equals("config-error")) throw new IllegalArgumentException("模型配置不完整，请设置 QUOTE_AI_CHAT_URL、QUOTE_AI_MODEL 和 QUOTE_AI_API_KEY。");
            try {
                String instructions = loaded.stream().map(Skill::content).reduce("", (a, b) -> a + "\n\n" + b);
                String context = mapper.writeValueAsString(Map.of("request", request, "catalog", data));
                var payload = Map.of("model", model, "thinking", Map.of("type", "disabled"), "response_format", Map.of("type", "json_object"), "max_tokens", 1200, "messages", List.of(
                    Map.of("role", "system", "content", instructions), Map.of("role", "user", "content", context)));
                var builder = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(55))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload)));
                if (present(key)) builder.header("Authorization", "Bearer " + key);
                var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
                var response = client.send(builder.build(), HttpResponse.BodyHandlers.ofInputStream());
                String body;
                try (var stream = response.body()) {
                    byte[] bytes = stream.readNBytes(65537);
                    if (bytes.length > 65536) throw new IllegalArgumentException("模型返回过大，请缩短回复。");
                    body = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
                }
                if (response.statusCode() != 200) throw new IllegalArgumentException("模型接口返回 HTTP " + response.statusCode() + "，本次未生成建议。");
                String content = mapper.readTree(body).path("choices").path(0).path("message").path("content").asText();
                JsonNode answer = mapper.readTree(content);
                if (answer == null || !answer.isObject() || answer.size() != 2 || !answer.path("targetMargin").isNumber()
                    || !answer.path("reason").isTextual() || answer.path("reason").asText().isBlank())
                    throw new IllegalArgumentException("模型必须只返回 targetMargin 和 reason，本次未应用任何修改。");
                after = answer.get("targetMargin").decimalValue();
                reason = answer.get("reason").asText();
                if (reason.length() > 1500) throw new IllegalArgumentException("模型说明超过 1500 字。");
            } catch (IllegalArgumentException e) { throw e; }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new IllegalArgumentException("模型请求中断，本次未应用修改。"); }
            catch (Exception e) { throw new IllegalArgumentException("模型请求失败或返回格式不正确，本次未应用修改。"); }
        // 即使模型声称可以修改，也必须经过和手工报价完全相同的后端校验。
        engine.validate(new Request(request.items(), request.regionId(), after, request.budget(), request.objective(), request.note()), data);
        return new Suggestion(mode, request.targetMargin(), after, reason, loaded.stream().map(Skill::id).toList(), "targetMargin");
    }
}
