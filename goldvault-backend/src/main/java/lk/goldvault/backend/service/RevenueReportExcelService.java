package lk.goldvault.backend.service;

import lk.goldvault.backend.dto.response.RevenueReportResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * Renders the admin revenue & shop-performance report as an .xlsx workbook —
 * a "Summary" sheet with the headline figures, and a "By Shop" sheet with the
 * full per-shop breakdown for further analysis in Excel/Sheets.
 */
@Service
public class RevenueReportExcelService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public byte[] generateExcel(RevenueReportResponse report) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CellStyle titleStyle   = titleStyle(workbook);
            CellStyle labelStyle   = labelStyle(workbook);
            CellStyle headerStyle  = headerStyle(workbook);
            CellStyle currencyStyle = currencyStyle(workbook);
            CellStyle textStyle    = workbook.createCellStyle();

            buildSummarySheet(workbook, report, titleStyle, labelStyle, currencyStyle, textStyle);
            buildShopSheet(workbook, report, headerStyle, currencyStyle);

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate revenue report Excel: " + e.getMessage(), e);
        }
    }

    // ── Summary sheet ────────────────────────────────────────────────────────────

    private void buildSummarySheet(Workbook wb, RevenueReportResponse report, CellStyle titleStyle,
                                    CellStyle labelStyle, CellStyle currencyStyle, CellStyle textStyle) {
        Sheet sheet = wb.createSheet("Summary");
        int r = 0;

        Row titleRow = sheet.createRow(r++);
        cell(titleRow, 0, "GoldVault Platform — Revenue & Shop Performance Report", titleStyle);

        Row periodRow = sheet.createRow(r++);
        cell(periodRow, 0, "Period", labelStyle);
        cell(periodRow, 1, report.getStartDate().format(DATE_FMT) + " – " + report.getEndDate().format(DATE_FMT), textStyle);

        r++; // blank row

        r = summaryRow(sheet, r, "Total payment volume (LKR)", report.getTotalPaymentVolume().doubleValue(), labelStyle, currencyStyle);
        r = summaryRow(sheet, r, "Platform commission (LKR)",  report.getTotalCommission().doubleValue(),   labelStyle, currencyStyle);

        Row paymentsRow = sheet.createRow(r++);
        cell(paymentsRow, 0, "Total payments", labelStyle);
        cell(paymentsRow, 1, report.getTotalPaymentCount(), textStyle);

        Row shopsRow = sheet.createRow(r++);
        cell(shopsRow, 0, "Active shops", labelStyle);
        cell(shopsRow, 1, report.getActiveShopCount(), textStyle);

        sheet.setColumnWidth(0, 32 * 256);
        sheet.setColumnWidth(1, 28 * 256);
    }

    private int summaryRow(Sheet sheet, int r, String label, double value, CellStyle labelStyle, CellStyle currencyStyle) {
        Row row = sheet.createRow(r);
        cell(row, 0, label, labelStyle);
        cell(row, 1, value, currencyStyle);
        return r + 1;
    }

    // ── By-shop sheet ────────────────────────────────────────────────────────────

    private void buildShopSheet(Workbook wb, RevenueReportResponse report, CellStyle headerStyle, CellStyle currencyStyle) {
        Sheet sheet = wb.createSheet("By Shop");

        Row header = sheet.createRow(0);
        String[] headers = {"Shop", "Payment Volume (LKR)", "Commission Owed (LKR)", "Payments"};
        for (int c = 0; c < headers.length; c++) {
            cell(header, c, headers[c], headerStyle);
        }

        int r = 1;
        for (RevenueReportResponse.ShopRevenueBreakdown row : report.getByShop()) {
            Row dataRow = sheet.createRow(r++);
            dataRow.createCell(0).setCellValue(row.getShopName());
            Cell volCell = dataRow.createCell(1);
            volCell.setCellValue(row.getPaymentVolume().doubleValue());
            volCell.setCellStyle(currencyStyle);
            Cell commCell = dataRow.createCell(2);
            commCell.setCellValue(row.getCommissionOwed().doubleValue());
            commCell.setCellStyle(currencyStyle);
            dataRow.createCell(3).setCellValue(row.getPaymentCount());
        }

        // Fixed widths rather than autoSizeColumn() — autosizing needs AWT font
        // metrics, which can throw on minimal/headless Docker images without
        // fontconfig installed.
        sheet.setColumnWidth(0, 30 * 256);
        sheet.setColumnWidth(1, 22 * 256);
        sheet.setColumnWidth(2, 22 * 256);
        sheet.setColumnWidth(3, 12 * 256);
    }

    // ── Styles ───────────────────────────────────────────────────────────────────

    private CellStyle titleStyle(Workbook wb) {
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        CellStyle style = wb.createCellStyle();
        style.setFont(font);
        return style;
    }

    private CellStyle labelStyle(Workbook wb) {
        Font font = wb.createFont();
        font.setBold(true);
        CellStyle style = wb.createCellStyle();
        style.setFont(font);
        return style;
    }

    private CellStyle headerStyle(Workbook wb) {
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        CellStyle style = wb.createCellStyle();
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle currencyStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setDataFormat(wb.createDataFormat().getFormat("#,##0.00"));
        return style;
    }

    private void cell(Row row, int col, String value, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(value);
        c.setCellStyle(style);
    }

    private void cell(Row row, int col, double value, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(value);
        c.setCellStyle(style);
    }

    private void cell(Row row, int col, long value, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(value);
        c.setCellStyle(style);
    }
}