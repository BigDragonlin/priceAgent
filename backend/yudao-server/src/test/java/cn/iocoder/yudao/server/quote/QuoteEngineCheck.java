package cn.iocoder.yudao.server.quote;

import java.math.BigDecimal;
import java.io.ByteArrayInputStream;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import static cn.iocoder.yudao.server.quote.QuoteTypes.*;

/** 可独立运行的业务验收：按手算金额验证，不需要数据库或外部模型。真实模型另做接口验收。 */
public class QuoteEngineCheck {
    static BigDecimal n(String n) { return new BigDecimal(n); }
    static void check(boolean ok, String reason) { if (!ok) throw new AssertionError(reason); }
    static void equal(BigDecimal actual, String expected) { check(actual.compareTo(n(expected)) == 0, actual + " != " + expected); }
    static Request request(List<Item> items, String margin, String budget) {
        return new Request(items, "province", n(margin), n(budget), "balanced", "");
    }
    static void reject(Runnable action) {
        try { action.run(); throw new AssertionError("应拒绝错误参数"); } catch (IllegalArgumentException expected) { }
    }
    public static void main(String[] args) throws Exception {
        QuoteEngine engine = new QuoteEngine(new DemoQuoteDataProvider());
        var items = List.of(new Item("aluminum", 100));
        Result result = engine.calculate(request(items, "25", "0"));
        equal(result.cost(), "9292.00"); equal(result.freight(), "156.00");
        equal(result.lines().get(0).unitPrice(), "123.90"); equal(result.net(), "12390.00");
        equal(result.tax(), "1610.70"); equal(result.total(), "14000.70"); equal(result.profit(), "3098.00");
        check(result.feasible(), "正常报价必须可行");
        check(engine.calculate(request(items, "25", "14000.70")).feasible(), "等于预算应允许");
        check(!engine.calculate(request(items, "25", "14000.69")).feasible(), "超预算一分钱也应报冲突");
        reject(() -> engine.calculate(request(items, "19.99", "0")));
        reject(() -> engine.calculate(request(items, "60.01", "0")));
        reject(() -> engine.calculate(request(items, "25", "-1")));
        reject(() -> engine.calculate(request(List.of(new Item("missing", 1)), "25", "0")));
        reject(() -> engine.calculate(request(List.of(new Item("aluminum", 0)), "25", "0")));
        reject(() -> engine.calculate(request(List.of(new Item("aluminum", 1), new Item("aluminum", 2)), "25", "0")));
        for (int quantity : new int[]{1, 3, 99, 100000}) {
            Result multi = engine.calculate(request(List.of(new Item("aluminum", quantity), new Item("steel", 7), new Item("panel", 13)), "20", "0"));
            equal(multi.lines().stream().map(Line::allocatedFreight).reduce(BigDecimal.ZERO, BigDecimal::add), multi.freight().toPlainString());
            equal(multi.lines().stream().map(Line::cost).reduce(BigDecimal.ZERO, BigDecimal::add), multi.cost().toPlainString());
            equal(multi.lines().stream().map(Line::amount).reduce(BigDecimal.ZERO, BigDecimal::add), multi.net().toPlainString());
            check(multi.profit().multiply(n("100")).compareTo(multi.net().multiply(n("20"))) >= 0, "舍入不能突破底线");
        }
        ObjectMapper mapper = new ObjectMapper();
        try { mapper.readValue("{\"productId\":\"aluminum\",\"quantity\":1.9}", Item.class); throw new AssertionError("不得截断小数数量"); }
        catch (com.fasterxml.jackson.core.JsonProcessingException expected) { }
        check(new QuoteSkills().load().size() == 4, "必须读到总 Skill 与三个子 Skill");
        check(new QuoteSuggestionService(engine, new QuoteSkills(), null, null, null).mode().equals("config-error"), "不得模拟模型");
        reject(() -> new QuoteSuggestionService(engine, new QuoteSkills(), null, null, null).suggest(request(items, "25", "0")));
        var excel = new QuoteExcel();
        try (var customer = new XSSFWorkbook(new ByteArrayInputStream(excel.export(result, false)))) {
            check(customer.getNumberOfSheets() == 1, "客户版不得含内部表");
            equal(BigDecimal.valueOf(customer.getSheetAt(0).getRow(6).getCell(5).getNumericCellValue()), "14000.70");
            for (var row : customer.getSheetAt(0)) for (var cell : row) {
                check(!cell.toString().contains("利润") && !cell.toString().contains("成本"), "客户版泄露内部信息");
            }
        }
        try (var internal = new XSSFWorkbook(new ByteArrayInputStream(excel.export(result, true)))) {
            check(internal.getNumberOfSheets() == 2, "内部版必须含测算表");
            equal(BigDecimal.valueOf(internal.getSheet("内部测算").getRow(6).getCell(1).getNumericCellValue()), "9292");
        }
        System.out.println("PASS: 手算金额、预算边界、参数拒绝、运费分摊、毛利底线、Skill、无 Mock 和 Excel 验收全部通过");
    }
}
