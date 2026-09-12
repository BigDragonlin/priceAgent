package cn.iocoder.yudao.server.quote;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import static cn.iocoder.yudao.server.quote.QuoteTypes.*;

/** 四个独立计算单元，由 QuoteEngine 固定编排；Skill 只能建议参数，不能执行公式代码。 */
public final class CalculationUnits {
    static final BigDecimal HUNDRED = new BigDecimal("100");
    static BigDecimal money(BigDecimal value) { return value.setScale(2, RoundingMode.HALF_UP); }
    interface Unit { void calculate(Context c); }
    static final class Context {
        final Request request;
        final Catalog catalog;
        final List<BigDecimal> goodsCosts = new ArrayList<>();
        final List<Line> lines = new ArrayList<>();
        final List<Step> steps = new ArrayList<>();
        BigDecimal goods = BigDecimal.ZERO, weight = BigDecimal.ZERO, freight, cost, net, tax, total;
        Context(Request request, Catalog catalog) { this.request = request; this.catalog = catalog; }
        Product product(String id) { return catalog.products().stream().filter(p -> p.id().equals(id)).findFirst().orElseThrow(); }
    }
    /** 原料损耗只计入原料；加工和包装不重复乘损耗率。 */
    static final class CostUnit implements Unit {
        public void calculate(Context c) {
            for (Item item : c.request.items()) {
                Product p = c.product(item.productId());
                BigDecimal quantity = BigDecimal.valueOf(item.quantity());
                BigDecimal unitCost = p.material().multiply(BigDecimal.ONE.add(c.catalog.rules().lossRate().divide(HUNDRED)))
                    .add(p.processing()).add(p.packaging());
                BigDecimal cost = money(unitCost.multiply(quantity));
                c.goodsCosts.add(cost);
                c.goods = c.goods.add(cost);
                c.weight = c.weight.add(p.weightKg().multiply(quantity));
                c.steps.add(new Step("成本 · " + p.name(), "(原料 " + p.material() + " × (1 + 损耗 " + c.catalog.rules().lossRate()
                    + "%）+ 加工 " + p.processing() + " + 包装 " + p.packaging() + ") × " + item.quantity(), cost));
            }
        }
    }
    /** 运输费用按整单重量计算一次；后续分摊进产品售价，不额外重复收费。 */
    static final class FreightUnit implements Unit {
        public void calculate(Context c) {
            Region region = c.catalog.regions().stream().filter(r -> r.id().equals(c.request.regionId())).findFirst().orElseThrow();
            c.freight = money(region.baseFee().add(region.perKg().multiply(c.weight)));
            c.cost = c.goods.add(c.freight);
            c.steps.add(new Step("运输", "起步费 " + region.baseFee() + " + " + c.weight + " kg × " + region.perKg(), c.freight));
        }
    }
    /** 按数量分摊运费，最后一行承担分摊尾差；单价向上取分，避免舍入突破毛利底线。 */
    static final class MarginUnit implements Unit {
        public void calculate(Context c) {
            int quantity = c.request.items().stream().mapToInt(Item::quantity).sum();
            BigDecimal remaining = c.freight;
            c.net = BigDecimal.ZERO;
            for (int i = 0; i < c.request.items().size(); i++) {
                Item item = c.request.items().get(i);
                Product p = c.product(item.productId());
                BigDecimal share = i == c.request.items().size() - 1 ? remaining
                    : c.freight.multiply(BigDecimal.valueOf(item.quantity())).divide(BigDecimal.valueOf(quantity), 2, RoundingMode.DOWN);
                remaining = remaining.subtract(share);
                BigDecimal lineCost = c.goodsCosts.get(i).add(share);
                BigDecimal divisor = BigDecimal.valueOf(item.quantity()).multiply(BigDecimal.ONE.subtract(c.request.targetMargin().divide(HUNDRED)));
                BigDecimal price = lineCost.divide(divisor, 2, RoundingMode.CEILING);
                BigDecimal amount = price.multiply(BigDecimal.valueOf(item.quantity()));
                c.lines.add(new Line(p.id(), p.name(), p.spec(), item.quantity(), lineCost, share, price, amount));
                c.net = c.net.add(amount);
                c.steps.add(new Step("利润定价 · " + p.name(), "含分摊运费成本 " + lineCost + " ÷ " + item.quantity()
                    + " ÷ (1 − " + c.request.targetMargin() + "%)，单价向上取分后 × 数量", amount));
            }
        }
    }
    /** Demo 使用统一税率；税额在整单上舍入，毛利按不含税收入计算。 */
    static final class TaxUnit implements Unit {
        public void calculate(Context c) {
            c.tax = money(c.net.multiply(c.catalog.rules().taxRate()).divide(HUNDRED));
            c.total = c.net.add(c.tax);
            c.steps.add(new Step("税费", c.net + " × " + c.catalog.rules().taxRate() + "%", c.tax));
            c.steps.add(new Step("含税合计", c.net + " + " + c.tax, c.total));
        }
    }
}
