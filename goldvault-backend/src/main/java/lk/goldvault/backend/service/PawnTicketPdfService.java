package lk.goldvault.backend.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lk.goldvault.backend.entity.GoldItem;
import lk.goldvault.backend.entity.PawnTicket;
import lk.goldvault.backend.repository.PawnTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PawnTicketPdfService {

    private final PawnTicketRepository pawnTicketRepository;
    private final InterestCalculatorService interestCalculatorService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final Font FONT_TITLE   = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD,   new BaseColor(0x2C, 0x2C, 0x2C));
    private static final Font FONT_HEADING = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD,   new BaseColor(0x2C, 0x2C, 0x2C));
    private static final Font FONT_BODY    = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, new BaseColor(0x44, 0x44, 0x44));
    private static final Font FONT_SMALL   = new Font(Font.FontFamily.HELVETICA,  9, Font.NORMAL, new BaseColor(0x77, 0x77, 0x77));
    private static final Font FONT_LABEL   = new Font(Font.FontFamily.HELVETICA,  9, Font.BOLD,   new BaseColor(0x88, 0x88, 0x88));
    private static final Font FONT_MONO    = new Font(Font.FontFamily.COURIER,   11, Font.BOLD,   new BaseColor(0x1A, 0x1A, 0x1A));
    private static final Font FONT_AMOUNT  = new Font(Font.FontFamily.COURIER,   13, Font.BOLD,   new BaseColor(0x1D, 0x4E, 0x89));
    private static final Font FONT_FOOTER  = new Font(Font.FontFamily.HELVETICA,  8, Font.ITALIC, new BaseColor(0xAA, 0xAA, 0xAA));

    private static final BaseColor COLOR_GOLD      = new BaseColor(0xC9, 0x9A, 0x06);
    private static final BaseColor COLOR_HEADER_BG = new BaseColor(0x1A, 0x1A, 0x2E);
    private static final BaseColor COLOR_ROW_ALT   = new BaseColor(0xF8, 0xF6, 0xF0);
    private static final BaseColor COLOR_BORDER    = new BaseColor(0xDD, 0xD8, 0xCC);
    private static final BaseColor COLOR_SECTION   = new BaseColor(0xF2, 0xEF, 0xE8);

    public byte[] generateReceiptPdf(Long ticketId) {
        PawnTicket ticket = pawnTicketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + ticketId));

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter writer = PdfWriter.getInstance(doc, out);

            doc.open();

            addHeader(doc, ticket);
            addDivider(doc, COLOR_GOLD);
            addTicketInfo(doc, ticket);
            addDivider(doc, COLOR_BORDER);
            addGoldItemsTable(doc, ticket.getGoldItems());
            addDivider(doc, COLOR_BORDER);
            addFinancialSummary(doc, ticket);
            addQrCode(doc, writer, ticket);
            addFooter(doc, ticket);

            doc.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }

    // ── Header ──────────────────────────────────────────────────────────────────

    private void addHeader(Document doc, PawnTicket ticket) throws DocumentException {
        // Shop name banner
        PdfPTable banner = new PdfPTable(1);
        banner.setWidthPercentage(100);

        PdfPCell nameCell = new PdfPCell();
        nameCell.setBackgroundColor(COLOR_HEADER_BG);
        nameCell.setBorder(Rectangle.NO_BORDER);
        nameCell.setPadding(18);

        Paragraph shopName = new Paragraph(ticket.getShop().getName().toUpperCase(), FONT_TITLE);
        shopName.setAlignment(Element.ALIGN_CENTER);
        shopName.setSpacingBefore(0);

        Font goldFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, COLOR_GOLD);
        Paragraph subLine = new Paragraph("PAWN TICKET RECEIPT  •  GoldVault Platform", goldFont);
        subLine.setAlignment(Element.ALIGN_CENTER);

        nameCell.addElement(shopName);
        nameCell.addElement(subLine);
        banner.addCell(nameCell);
        doc.add(banner);

        doc.add(Chunk.NEWLINE);

        // Shop contact info line
        String contactLine = buildContactLine(ticket);
        if (!contactLine.isEmpty()) {
            Paragraph contact = new Paragraph(contactLine, FONT_SMALL);
            contact.setAlignment(Element.ALIGN_CENTER);
            doc.add(contact);
        }

        doc.add(Chunk.NEWLINE);
    }

    private String buildContactLine(PawnTicket ticket) {
        StringBuilder sb = new StringBuilder();
        if (ticket.getShop().getAddress() != null) sb.append(ticket.getShop().getAddress());
        if (ticket.getShop().getPhone() != null) {
            if (sb.length() > 0) sb.append("  |  ");
            sb.append(ticket.getShop().getPhone());
        }
        if (ticket.getShop().getEmail() != null) {
            if (sb.length() > 0) sb.append("  |  ");
            sb.append(ticket.getShop().getEmail());
        }
        return sb.toString();
    }

    // ── Ticket info two-column section ──────────────────────────────────────────

    private void addTicketInfo(Document doc, PawnTicket ticket) throws DocumentException {
        doc.add(sectionHeading("TICKET DETAILS"));

        PdfPTable info = new PdfPTable(new float[]{1f, 1f});
        info.setWidthPercentage(100);
        info.setSpacingBefore(6);
        info.setSpacingAfter(6);

        addInfoRow(info, "Ticket number",  ticket.getTicketNumber());
        addInfoRow(info, "Status",         ticket.getStatus().name());
        addInfoRow(info, "Customer name",  ticket.getCustomer().getFullName());
        addInfoRow(info, "NIC",            ticket.getCustomer().getNic());
        addInfoRow(info, "Phone",          ticket.getCustomer().getPhone() != null ? ticket.getCustomer().getPhone() : "—");
        addInfoRow(info, "Address",        ticket.getCustomer().getAddress() != null ? ticket.getCustomer().getAddress() : "—");
        addInfoRow(info, "Pawn date",      ticket.getPawnDate().format(DATE_FMT));
        addInfoRow(info, "Due date",       ticket.getExpiryDate().format(DATE_FMT));

        if (ticket.getBranch() != null) {
            addInfoRow(info, "Branch", ticket.getBranch().getName());
        }

        doc.add(info);
    }

    private void addInfoRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label.toUpperCase(), FONT_LABEL));
        labelCell.setBorder(Rectangle.BOTTOM);
        labelCell.setBorderColor(COLOR_BORDER);
        labelCell.setPadding(6);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, FONT_BODY));
        valueCell.setBorder(Rectangle.BOTTOM);
        valueCell.setBorderColor(COLOR_BORDER);
        valueCell.setPadding(6);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    // ── Gold items table ─────────────────────────────────────────────────────────

    private void addGoldItemsTable(Document doc, List<GoldItem> items) throws DocumentException {
        doc.add(sectionHeading("PAWNED GOLD ITEMS"));

        PdfPTable table = new PdfPTable(new float[]{3f, 1.5f, 1f, 1.5f, 2f});
        table.setWidthPercentage(100);
        table.setSpacingBefore(6);
        table.setSpacingAfter(6);

        // Header row
        String[] headers = {"Description", "Type", "Purity", "Weight (g)", "Est. Value (LKR)"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, FONT_LABEL));
            cell.setBackgroundColor(COLOR_SECTION);
            cell.setBorder(Rectangle.BOX);
            cell.setBorderColor(COLOR_BORDER);
            cell.setPadding(7);
            table.addCell(cell);
        }

        boolean alt = false;
        for (GoldItem item : items) {
            BaseColor rowBg = alt ? COLOR_ROW_ALT : BaseColor.WHITE;
            addItemCell(table, item.getDescription(),                    rowBg, Element.ALIGN_LEFT);
            addItemCell(table, item.getGoldType().name(),                rowBg, Element.ALIGN_CENTER);
            addItemCell(table, item.getPurity().name(),                  rowBg, Element.ALIGN_CENTER);
            addItemCell(table, item.getWeightGrams().toPlainString(),    rowBg, Element.ALIGN_CENTER);
            addItemCell(table, item.getEstimatedValue() != null
                    ? "LKR " + formatAmount(item.getEstimatedValue())
                    : "—",                                               rowBg, Element.ALIGN_RIGHT);
            alt = !alt;
        }

        doc.add(table);
    }

    private void addItemCell(PdfPTable table, String text, BaseColor bg, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_BODY));
        cell.setBackgroundColor(bg);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(COLOR_BORDER);
        cell.setPadding(6);
        cell.setHorizontalAlignment(align);
        table.addCell(cell);
    }

    // ── Financial summary ────────────────────────────────────────────────────────

    private void addFinancialSummary(Document doc, PawnTicket ticket) throws DocumentException {
        doc.add(sectionHeading("FINANCIAL SUMMARY"));

        var today = java.time.LocalDate.now();
        BigDecimal outstanding = interestCalculatorService.calculateOutstandingBalance(ticket, today);
        BigDecimal totalPaid   = interestCalculatorService.totalPaid(ticket);

        PdfPTable table = new PdfPTable(new float[]{2f, 1f});
        table.setWidthPercentage(60);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.setSpacingBefore(6);
        table.setSpacingAfter(8);

        addSummaryRow(table, "Loan amount",         "LKR " + formatAmount(ticket.getLoanAmount()),  false);
        addSummaryRow(table, "Interest rate",       ticket.getInterestRate() + "% (" + ticket.getInterestType() + ")", false);
        addSummaryRow(table, "Total paid to date",  "LKR " + formatAmount(totalPaid),               false);
        addSummaryRow(table, "Outstanding balance", "LKR " + formatAmount(outstanding),             true);

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

    // ── QR Code ──────────────────────────────────────────────────────────────────

    private void addQrCode(Document doc, PdfWriter writer, PawnTicket ticket) throws DocumentException {
        if (ticket.getQrCode() == null || ticket.getQrCode().isEmpty()) return;

        try {
            byte[] qrBytes = Base64.getDecoder().decode(ticket.getQrCode());
            Image qrImage = Image.getInstance(qrBytes);
            qrImage.scaleToFit(90, 90);
            qrImage.setAlignment(Image.RIGHT);

            Paragraph qrLabel = new Paragraph("Scan to verify ticket", FONT_SMALL);
            qrLabel.setAlignment(Element.ALIGN_RIGHT);
            doc.add(qrLabel);
            doc.add(qrImage);
            doc.add(Chunk.NEWLINE);
        } catch (Exception ignored) {
            // QR decode failed — skip silently, PDF still generates
        }
    }

    // ── Footer ───────────────────────────────────────────────────────────────────

    private void addFooter(Document doc, PawnTicket ticket) throws DocumentException {
        doc.add(Chunk.NEWLINE);
        addDivider(doc, COLOR_BORDER);

        if (ticket.getNotes() != null && !ticket.getNotes().isBlank()) {
            Paragraph notes = new Paragraph("Notes: " + ticket.getNotes(), FONT_SMALL);
            notes.setSpacingBefore(4);
            doc.add(notes);
        }

        Paragraph footer = new Paragraph(
            "This receipt is computer generated and valid without signature.  " +
            "Generated on: " + java.time.LocalDate.now().format(DATE_FMT) +
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