package cn.iocoder.yudao.server.quote;

import java.math.BigDecimal;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;

/** 报价的输入输出合同。金额使用十进制数，避免小数相加出现误差。 */
public final class QuoteTypes {
    private QuoteTypes() {}
    public record Product(String id, String name, String spec, BigDecimal material,
                          BigDecimal processing, BigDecimal packaging, BigDecimal weightKg) {}
    public record Region(String id, String name, BigDecimal baseFee, BigDecimal perKg) {}
    public record Rules(BigDecimal minMargin, BigDecimal maxMargin, BigDecimal lossRate,
                        BigDecimal taxRate, String version) {}
    public record Catalog(List<Product> products, List<Region> regions, Rules rules, String source) {}
    public record Item(String productId, @JsonDeserialize(using = StrictQuantityDeserializer.class) int quantity) {}
    public record Request(List<Item> items, String regionId, BigDecimal targetMargin,
                          BigDecimal budget, String objective, String note) {}
    public record Step(String unit, String formula, BigDecimal amount) {}
    public record Line(String productId, String name, String spec, int quantity,
                       BigDecimal cost, BigDecimal allocatedFreight, BigDecimal unitPrice,
                       BigDecimal amount) {}
    public record Result(String id, String createdAt, String formulaVersion, Request parameters,
                         Catalog catalog, List<Line> lines, List<Step> steps, BigDecimal cost,
                         BigDecimal freight, BigDecimal net, BigDecimal tax, BigDecimal total,
                         BigDecimal profit, BigDecimal actualMargin, boolean feasible,
                         List<String> conflicts) {}
    public record Skill(String id, String title, String content) {}
    public record Suggestion(String mode, BigDecimal before, BigDecimal after, String reason,
                             List<String> skills, String field) {}
}
