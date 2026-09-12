package cn.iocoder.yudao.server.quote;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import static cn.iocoder.yudao.server.quote.QuoteTypes.*;

/** 从总 Skill 的本地链接读取子 Skill。只允许打包资源，禁止任意路径或网络文件。 */
public final class QuoteSkills {
    private static String read(String path) {
        try (var input = QuoteSkills.class.getResourceAsStream("/quote-skills/" + path)) {
            if (input == null) throw new IllegalStateException("缺少报价 Skill：" + path);
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) { throw new IllegalStateException("读取报价 Skill 失败", e); }
    }
    public List<Skill> load() {
        String root = read("SKILL.md");
        List<Skill> result = new ArrayList<>();
        result.add(new Skill("main", "报价总 Skill", root));
        var matcher = Pattern.compile("\\]\\(([a-z-]+)/SKILL\\.md\\)").matcher(root);
        Set<String> seen = new HashSet<>();
        while (matcher.find()) {
            String name = matcher.group(1);
            if (seen.add(name)) result.add(new Skill(name, name, read(name + "/SKILL.md")));
        }
        if (result.size() == 1) throw new IllegalStateException("总 Skill 未引用任何子 Skill。");
        return List.copyOf(result);
    }
}
