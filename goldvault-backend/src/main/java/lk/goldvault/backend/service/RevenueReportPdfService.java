package lk.goldvault.backend.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import lk.goldvault.backend.dto.response.RevenueReportResponse;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Renders the admin revenue & shop-performance report as a PDF.
 * Visual theme mirrors {@link PawnTicketPdfService} for consistency
 * across the app's generated documents.
 */
@Service
public class RevenueReportPdfService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private static final Font FONT_TITLE   = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD,   new BaseColor(0x2C, 0x2C, 0x2C));
    private static final Font FONT_HEADING = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD,   new BaseColor(0x2C, 0x2C, 0x2C));
    private static final Font FONT_BODY    = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, new BaseColor(0x44, 0x44, 0x44));
    private static final Font FONT_SMALL   = new Font(Font.FontFamily.HELVETICA,  9, Font.NORMAL, new BaseColor(0x77, 0x77, 0x77));
    private static final Font FONT_LABEL   = new Font(Font.FontFamily.HELVETICA,  9, Font.BOLD,   new BaseColor(0x88, 0x88, 0x88));
    private static final Font FONT_AMOUNT  = new Font(Font.FontFamily.COURIER,   13, Font.BOLD,   new BaseColor(0x1D, 0x4E, 0x89));
    private static final Font FONT_FOOTER  = new Font(Font.FontFamily.HELVETICA,  8, Font.ITALIC, new BaseColor(0xAA, 0xAA, 0xAA));

    private static final BaseColor COLOR_GOLD      = new BaseColor(0xC9, 0x9A, 0x06);
    private static final BaseColor COLOR_HEADER_BG = new BaseColor(0x1A, 0x1A, 0x2E);
    private static final BaseColor COLOR_ROW_ALT   = new BaseColor(0xF8, 0xF6, 0xF0);
    private static final BaseColor COLOR_BORDER    = new BaseColor(0xDD, 0xD8, 0xCC);
    private static final BaseColor COLOR_SECTION   = new BaseColor(0xF2, 0xEF, 0xE8);

    public byte[] generatePdf(RevenueReportResponse report) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(doc, out);

            doc.open();
            addHeader(doc, report);
            addDivider(doc, COLOR_GOLD);
            addSummary(doc, report);
            addDivider(doc, COLOR_BORDER);
            addShopBreakdownTable(doc, report);
            addFooter(doc);
            doc.close();

            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate revenue report PDF: " + e.getMessage(), e);
        }
    }

    // ── Header ──────────────────────────────────────────────────────────────────

    private void addHeader(Document doc, RevenueReportResponse report) throws DocumentException {
        PdfPTable banner = new PdfPTable(1);
        banner.setWidthPercentage(100);

        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(COLOR_HEADER_BG);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(18);

        Paragraph title = new Paragraph("GOLDVAULT PLATFORM", FONT_TITLE);
        title.setAlignment(Element.ALIGN_CENTER);

        Font goldFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, COLOR_GOLD);
        Paragraph subLine = new Paragraph("REVENUE & SHOP PERFORMANCE REPORT", goldFont);
        subLine.setAlignment(Element.ALIGN_CENTER);

        cell.addElement(title);
        cell.addElement(subLine);
        banner.addCell(cell);
        doc.add(banner);

        doc.add(Chunk.NEWLINE);

        Paragraph period = new Paragraph(
                "Period: " + report.getStartDate().format(DATE_FMT) + " — " + report.getEndDate().format(DATE_FMT),
                FONT_SMALL);
        period.setAlignment(Element.ALIGN_CENTER);
        doc.add(period);

        doc.add(Chunk.NEWLINE);
    }

    // ── Summary ─────────────────────────────────────────────────────────────────

    private void addSummary(Document doc, RevenueReportResponse report) throws DocumentException {
        doc.add(sectionHeading("SUMMARY"));

        PdfPTable table = new PdfPTable(new float[]{2f, 1f});
        table.setWidthPercentage(70);
        table.setSpacingBefore(6);
        table.setSpacingAfter(6);

        addSummaryRow(table, "Total payment volume", "LKR " + formatAmount(report.getTotalPaymentVolume()), true);
        addSummaryRow(table, "Platform commission",  "LKR " + formatAmount(report.getTotalCommission()), false);
        addSummaryRow(table, "Total payments",       String.valueOf(report.getTotalPaymentCount()), false);
        addSummaryRow(table, "Active shops",         String.valueOf(report.getActiveShopCount()), false);

        doc.add(table);
    }

    private void addSummaryRow(PdfPTable table, String label, String value, boolean highlight) {
        Font labelFont = highlight ? FONT_HEADING : FONT_BODY;
        Font valueFont = highlight ? FONT_AMOUNT  : FONT_BODY;
        BaseColor bg   = highlight ? COLOR_SECTION : BaseColor.WHITE;

        PdfPCell l = new PdfPCell(new Phrase(label, labelFont));
        l.setBackgroundColor(bg);
        l.setBorder(Rectangle.BOTTOM);
        l.setBorderColor(COLOR_BORDER);
        l.setPadding(7);

        PdfPCell v = new PdfPCell(new Phrase(value, valueFont));
        v.setBackgroundColor(bg);
        v.setBorder(Rectangle.BOTTOM);
        v.setBorderColor(COLOR_BORDER);
        v.setPadding(7);
        v.setHorizontalAlignment(Element.ALIGN_RIGHT);

        table.addCell(l);
        table.addCell(v);
    }

    // ── Shop breakdown ──────────────────────────────────────────────────────────

    private void addShopBreakdownTable(Document doc, RevenueReportResponse report) throws DocumentException {
        doc.add(sectionHeading("BREAKDOWN BY SHOP"));

        if (report.getByShop().isEmpty()) {
            Paragraph empty = new Paragraph("No payment activity in this period.", FONT_SMALL);
            empty.setSpacingBefore(4);
            doc.add(empty);
            return;
        }

        PdfPTable table = new PdfPTable(new float[]{3f, 1.6f, 1.6f, 1.2f});
        table.setWidthPercentage(100);
        table.setSpacingBefore(6);

        String[] headers = {"Shop", "Payment Volume (LKR)", "Commission Owed (LKR)", "Payments"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, FONT_LABEL));
            cell.setBackgroundColor(COLOR_SECTION);
            cell.setBorder(Rectangle.BOX);
            cell.setBorderColor(COLOR_BORDER);
            cell.setPadding(7);
            table.addCell(cell);
        }

        boolean alt = false;
        for (RevenueReportResponse.ShopRevenueBreakdown row : report.getByShop()) {
            BaseColor rowBg = alt ? COLOR_ROW_ALT : BaseColor.WHITE;
            addCell(table, row.getShopName(), rowBg, Element.ALIGN_LEFT);
            addCell(table, formatAmount(row.getPaymentVolume()), rowBg, Element.ALIGN_RIGHT);
            addCell(table, formatAmount(row.getCommissionOwed()), rowBg, Element.ALIGN_RIGHT);
            addCell(table, String.valueOf(row.getPaymentCount()), rowBg, Element.ALIGN_CENTER);
            alt = !alt;
        }

        doc.add(table);
    }

    private void addCell(PdfPTable table, String text, BaseColor bg, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_BODY));
        cell.setBackgroundColor(bg);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(COLOR_BORDER);
        cell.setPadding(6);
        cell.setHorizontalAlignment(align);
        table.addCell(cell);
    }

    // ── Footer ───────────────────────────────────────────────────────────────────

    private void addFooter(Document doc) throws DocumentException {
        doc.add(Chunk.NEWLINE);
        addDivider(doc, COLOR_BORDER);

        Paragraph footer = new Paragraph(
                "This report is computer generated.  Generated on: " + java.time.LocalDate.now().format(DATE_FMT) +
                        "  |  Powered by GoldVault Platform — goldvault.lk",
                FONT_FOOTER
        );
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(10);
        doc.add(footer);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private Paragraph sectionHeading(String text) {
        Font f = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, COLOR_GOLD);
        Paragraph p = new Paragraph(text, f);
        p.setSpacingBefore(12);
        p.setSpacingAfter(4);
        return p;
    }

    private void addDivider(Document doc, BaseColor color) throws DocumentException {
        LineSeparator line = new LineSeparator();
        line.setLineColor(color);
        line.setLineWidth(0.5f);
        doc.add(new Chunk(line));
        doc.add(Chunk.NEWLINE);
    }

    private String formatAmount(BigDecimal amount) {
        return NumberFormat.getNumberInstance(Locale.US).format(amount);
    }
}