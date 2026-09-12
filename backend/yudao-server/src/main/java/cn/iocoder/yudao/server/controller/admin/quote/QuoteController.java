package cn.iocoder.yudao.server.controller.admin.quote;

import cn.iocoder.yudao.server.quote.*;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.*;
import static cn.iocoder.yudao.server.quote.QuoteTypes.*;

/** 芋道管理接口：复用登录和租户隔离。Demo 快照只保存在内存，重启后需重新计算。 */
@RestController
@RequestMapping("/quote")
@PreAuthorize("isAuthenticated()")
public class QuoteController {
    private final QuoteEngine engine = new QuoteEngine(new DemoQuoteDataProvider());
    private final QuoteSkills skills = new QuoteSkills();
    private final QuoteSuggestionService suggestions = new QuoteSuggestionService(engine, skills);
    private record Stored(String owner, Result result) {}
    private final Map<String, Stored> snapshots = Collections.synchronizedMap(new LinkedHashMap<>());
    private String owner() { return TenantContextHolder.getTenantId() + ":" + SecurityFrameworkUtils.getLoginUserId(); }

    @GetMapping("/catalog")
    public CommonResult<Catalog> catalog() { return CommonResult.success(engine.catalog()); }
    @GetMapping("/skills")
    public CommonResult<Map<String, Object>> skills() { return CommonResult.success(Map.of("mode", suggestions.mode(), "model", suggestions.modelName(), "skills", skills.load())); }
    @PostMapping("/suggest")
    public CommonResult<Suggestion> suggest(@RequestBody Request request) { return CommonResult.success(suggestions.suggest(request)); }
    @PostMapping("/calculate")
    public CommonResult<Result> calculate(@RequestBody Request request) {
        Result result = engine.calculate(request);
        synchronized (snapshots) {
            // 防止演示服务长期运行时无限积累；淘汰最早快照不影响既有文件。
            if (snapshots.size() >= 1000) snapshots.remove(snapshots.keySet().iterator().next());
            snapshots.put(result.id(), new Stored(owner(), result));
        }
        return CommonResult.success(result);
    }
    @GetMapping("/{id}/excel")
    public void excel(@PathVariable String id, @RequestParam(defaultValue = "customer") String audience,
                      HttpServletResponse response) throws IOException {
        if (!Set.of("customer", "internal").contains(audience)) throw new IllegalArgumentException("未知的导出类型。");
        Stored stored = snapshots.get(id);
        if (stored == null || !stored.owner().equals(owner())) throw new IllegalArgumentException("报价不存在或已失效，请重新计算。");
        if (!stored.result().feasible()) throw new IllegalArgumentException("当前方案不满足约束，不能导出报价。");
        byte[] data = new QuoteExcel().export(stored.result(), audience.equals("internal"));
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=quote-" + audience + ".xlsx");
        response.getOutputStream().write(data);
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public CommonResult<Object> invalid(IllegalArgumentException error) {
        return CommonResult.error(400, error.getMessage());
    }
}
