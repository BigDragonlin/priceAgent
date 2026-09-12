package cn.iocoder.yudao.server.quote;

import java.math.BigDecimal;
import java.util.List;
import static cn.iocoder.yudao.server.quote.QuoteTypes.*;

/** Demo 业务表：所有示例成本由后端提供，浏览器和 AI 都无权覆盖。 */
public final class DemoQuoteDataProvider implements QuoteDataProvider {
    private static BigDecimal n(String n) { return new BigDecimal(n); }
    public Catalog catalog() {
        return new Catalog(List.of(
            new Product("aluminum", "铝合金外壳", "标准款 · 件", n("68"), n("18"), n("4"), n("0.8")),
            new Product("steel", "不锈钢支架", "加强款 · 件", n("42"), n("12"), n("3"), n("1.2")),
            new Product("panel", "控制面板", "基础款 · 件", n("110"), n("25"), n("6"), n("0.5"))
        ), List.of(
            new Region("local", "同城配送", n("30"), n("0.5")),
            new Region("province", "省内运输", n("60"), n("1.2")),
            new Region("national", "跨省运输", n("100"), n("2.5"))
        ), new Rules(n("20"), n("60"), n("2"), n("13"), "demo-cost-1"),
        "后端示例成本表；不含实时市场数据。金额为人民币，成本及单价均不含税。");
    }
}
