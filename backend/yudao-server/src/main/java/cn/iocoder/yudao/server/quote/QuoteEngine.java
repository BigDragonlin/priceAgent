package cn.iocoder.yudao.server.quote;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import static cn.iocoder.yudao.server.quote.QuoteTypes.*;
import static cn.iocoder.yudao.server.quote.CalculationUnits.*;

/** 报价入口：先严格核对输入，再依次运行计算单元，输出可复核的结果快照。 */
public final class QuoteEngine {
    private final QuoteDataProvider provider;
    private final List<Unit> units = List.of(new CostUnit(), new FreightUnit(), new MarginUnit(), new TaxUnit());
    public QuoteEngine(QuoteDataProvider provider) { this.provider = provider; }
    public Catalog catalog() { return provider.catalog(); }
    public void validate(Request r, Catalog catalog) {
        if (r == null || r.items() == null || r.items().isEmpty() || r.items().size() > 30)
            throw new IllegalArgumentException("请填写 1 至 30 行产品。");
        Set<String> ids = new HashSet<>();
        for (Item i : r.items()) {
            if (i == null || i.quantity() < 1 || i.quantity() > 100000) throw new IllegalArgumentException("数量必须为 1 至 100000 的整数。");
            if (catalog.products().stream().noneMatch(p -> p.id().equals(i.productId()))) throw new IllegalArgumentException("产品不存在，请重新选择。");
            if (!ids.add(i.productId())) throw new IllegalArgumentException("同一种产品请合并数量，不要重复添加。");
        }
        if (catalog.regions().stream().noneMatch(p -> p.id().equals(r.regionId()))) throw new IllegalArgumentException("请选择有效的运输地区。");
        if (r.targetMargin() == null || r.targetMargin().compareTo(catalog.rules().minMargin()) < 0
            || r.targetMargin().compareTo(catalog.rules().maxMargin()) > 0)
            throw new IllegalArgumentException("目标毛利率必须在 " + catalog.rules().minMargin() + "% 至 " + catalog.rules().maxMargin() + "% 之间。");
        if (r.targetMargin().stripTrailingZeros().scale() > 2) throw new IllegalArgumentException("毛利率最多保留两位小数。");
        if (r.budget() == null || r.budget().signum() < 0 || r.budget().compareTo(new BigDecimal("1000000000")) > 0)
            throw new IllegalArgumentException("含税预算须为 0 至 10 亿元；0 表示不限制。");
        if (r.budget().stripTrailingZeros().scale() > 2) throw new IllegalArgumentException("预算最多保留两位小数。");
        if (!Set.of("balanced", "competitive", "profit").contains(r.objective() == null ? "" : r.objective()))
            throw new IllegalArgumentException("请选择有效的报价目标。");
        if (r.note() != null && r.note().length() > 1000) throw new IllegalArgumentException("补充要求最多 1000 字。");
    }
    public Result calculate(Request r) {
        Catalog data = catalog();
        validate(r, data);
        Context c = new Context(r, data);
        units.forEach(unit -> unit.calculate(c));
        BigDecimal profit = c.net.subtract(c.cost);
        BigDecimal margin = profit.multiply(HUNDRED).divide(c.net, 4, RoundingMode.HALF_UP);
        List<String> conflicts = new ArrayList<>();
        if (r.budget().signum() > 0 && c.total.compareTo(r.budget()) > 0)
            conflicts.add("含税报价超过预算 " + money(c.total.subtract(r.budget())) + " 元。请调整预算、数量或允许范围内的毛利率。");
        if (profit.multiply(HUNDRED).compareTo(c.net.multiply(data.rules().minMargin())) < 0)
            conflicts.add("实际毛利低于后端底线，不能生成报价清单。");
        return new Result(UUID.randomUUID().toString(), Instant.now().toString(), "quote-formula-1", r, data,
            List.copyOf(c.lines), List.copyOf(c.steps), c.cost, c.freight, c.net, c.tax, c.total,
            profit, margin, conflicts.isEmpty(), List.copyOf(conflicts));
    }
}
