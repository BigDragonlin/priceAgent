package cn.iocoder.yudao.server.quote;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import java.io.*;
import java.math.BigDecimal;
import static cn.iocoder.yudao.server.quote.QuoteTypes.*;

/** 从后端结果快照导出真正的 xlsx。客户版不包含成本、利润和内部计算过程。 */
public final class QuoteExcel {
    private void row(Sheet sheet, int index, Object... values) {
        Row row = sheet.createRow(index);
        for (int i = 0; i < values.length; i++) {
            Cell cell = row.createCell(i);
            Object value = values[i];
            if (value instanceof Number n) cell.setCellValue(n.doubleValue());
            else cell.setCellValue(String.valueOf(value)); // 始终写文本，不把 = 开头的内容当公式。
        }
    }
    public byte[] export(Result result, boolean internal) throws IOException {
        try (XSSFWorkbook book = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet quote = book.createSheet("报价清单");
            row(quote, 0, "报价单", result.id());
            row(quote, 1, "币种", "人民币；单价及金额不含税，已包含运输费用");
            row(quote, 2, "产品", "规格", "数量", "单位", "不含税单价", "不含税金额");
            int n = 3;
            for (Line line : result.lines()) row(quote, n++, line.name(), line.spec(), line.quantity(), "件", line.unitPrice(), line.amount());
            row(quote, n++, "不含税合计", "", "", "", "", result.net());
            row(quote, n++, "税额（" + result.catalog().rules().taxRate() + "%）", "", "", "", "", result.tax());
            row(quote, n, "含税合计", "", "", "", "", result.total());
            quote.createFreezePane(0, 3);
            if (internal) {
                Sheet audit = book.createSheet("内部测算");
                row(audit, 0, "报价编号", result.id());
                row(audit, 1, "计算时间", result.createdAt());
                row(audit, 2, "公式版本", result.formulaVersion());
                row(audit, 3, "成本版本", result.catalog().rules().version());
                row(audit, 4, "目标毛利率（%）", result.parameters().targetMargin());
                row(audit, 5, "实际毛利率（%）", result.actualMargin());
                row(audit, 6, "总成本（含运输）", result.cost());
                row(audit, 7, "预计利润", result.profit());
                row(audit, 8, "计算单元", "计算过程", "结果（元）");
                n = 9;
                for (Step step : result.steps()) row(audit, n++, step.unit(), step.formula(), step.amount());
                row(audit, n++, "运输地区", result.parameters().regionId());
                row(audit, n++, "含税预算（0 为不限）", result.parameters().budget());
                row(audit, n++, "目标", result.parameters().objective());
                row(audit, n, "数据来源", result.catalog().source());
            }
            CellStyle header = book.createCellStyle();
            Font font = book.createFont(); font.setBold(true); header.setFont(font);
            CellStyle currency = book.createCellStyle(); currency.setDataFormat(book.createDataFormat().getFormat("0.00"));
            for (Sheet sheet : book) {
                for (Row r : sheet) for (Cell cell : r) {
                    if (r.getRowNum() == 0 || (sheet == quote && r.getRowNum() == 2)) cell.setCellStyle(header);
                    else if (cell.getCellType() == CellType.NUMERIC && !(sheet == quote && cell.getColumnIndex() == 2 && r.getRowNum() >= 3)) cell.setCellStyle(currency);
                }
                for (int col = 0; col < 6; col++) sheet.setColumnWidth(col, (col == 1 ? 65 : 24) * 256);
            }
            book.write(output);
            return output.toByteArray();
        }
    }
}
