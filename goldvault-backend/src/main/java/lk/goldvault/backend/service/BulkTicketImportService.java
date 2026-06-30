package lk.goldvault.backend.service;

import lk.goldvault.backend.dto.response.CsvImportRowResult;
import lk.goldvault.backend.dto.response.CsvImportSummaryResponse;
import lk.goldvault.backend.entity.Customer;
import lk.goldvault.backend.entity.GoldItem;
import lk.goldvault.backend.entity.PawnShop;
import lk.goldvault.backend.entity.PawnTicket;
import lk.goldvault.backend.enums.GoldPurity;
import lk.goldvault.backend.enums.GoldType;
import lk.goldvault.backend.enums.InterestType;
import lk.goldvault.backend.enums.TicketStatus;
import lk.goldvault.backend.repository.CustomerRepository;
import lk.goldvault.backend.repository.PawnShopRepository;
import lk.goldvault.backend.repository.PawnTicketRepository;
import lk.goldvault.backend.util.QrCodeUtil;
import lk.goldvault.backend.util.TicketNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BulkTicketImportService {

    private final PawnShopRepository    pawnShopRepository;
    private final CustomerRepository    customerRepository;
    private final PawnTicketRepository  pawnTicketRepository;
    private final TicketNumberGenerator ticketNumberGenerator;
    private final QrCodeUtil            qrCodeUtil;

    private static final DateTimeFormatter[] DATE_FORMATS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy")
    };

    /**
     * Expected CSV columns (header row required), in any order:
     * customerNic, customerName, customerPhone, loanAmount, interestRate, interestType,
     * periodMonths, pawnDate, itemDescription, goldType, weightGrams, purity, estimatedValue, notes
     *
     * If a customer with the given NIC already exists for this shop, that customer is reused.
     * If not, a new customer is created automatically using customerName/customerPhone.
     */
    @Transactional
    public CsvImportSummaryResponse importTickets(Long shopId, MultipartFile file) {
        PawnShop shop = pawnShopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found: " + shopId));

        List<CsvImportRowResult> results = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
            CSVParser parser = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setTrim(true)
                    .setIgnoreEmptyLines(true)
                    .build()
                    .parse(reader);

            int rowNum = 1; // header is row 0
            for (CSVRecord record : parser) {
                rowNum++;
                try {
                    String ticketNumber = importRow(shop, record);
                    results.add(CsvImportRowResult.builder()
                            .rowNumber(rowNum)
                            .success(true)
                            .ticketNumber(ticketNumber)
                            .customerNic(safeGet(record, "customerNic"))
                            .build());
                    successCount++;
                } catch (Exception rowEx) {
                    results.add(CsvImportRowResult.builder()
                            .rowNumber(rowNum)
                            .success(false)
                            .customerNic(safeGet(record, "customerNic"))
                            .errorMessage(rowEx.getMessage())
                            .build());
                    failureCount++;
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Could not parse CSV file: " + e.getMessage(), e);
        }

        return CsvImportSummaryResponse.builder()
                .totalRows(results.size())
                .successCount(successCount)
                .failureCount(failureCount)
                .results(results)
                .build();
    }

    // ── Per-row import ───────────────────────────────────────────────────────────

    private String importRow(PawnShop shop, CSVRecord record) {
        String nic = required(record, "customerNic");

        Customer customer = customerRepository.findByNic(nic).orElse(null);

        if (customer == null) {
            // Auto-create the customer if not found
            String name  = required(record, "customerName");
            String phone = safeGet(record, "customerPhone");

            customer = Customer.builder()
                    .shop(shop)
                    .fullName(name)
                    .nic(nic)
                    .phone(phone)
                    .kycStatus(lk.goldvault.backend.enums.KycStatus.PENDING)
                    .build();
            customer = customerRepository.save(customer);
        } else if (!customer.getShop().getId().equals(shop.getId())) {
            throw new RuntimeException("NIC " + nic + " belongs to a customer of another shop.");
        }

        BigDecimal loanAmount   = requiredDecimal(record, "loanAmount");
        BigDecimal interestRate = requiredDecimal(record, "interestRate");
        InterestType interestType = parseInterestType(safeGet(record, "interestType"));
        int periodMonths = requiredInt(record, "periodMonths");

        LocalDate pawnDate = parseDateOrToday(safeGet(record, "pawnDate"));
        LocalDate expiryDate = pawnDate.plusMonths(periodMonths);

        String itemDescription = required(record, "itemDescription");
        GoldType goldType = parseGoldType(safeGet(record, "goldType"));
        BigDecimal weightGrams = requiredDecimal(record, "weightGrams");
        GoldPurity purity = parseGoldPurity(safeGet(record, "purity"));
        BigDecimal estimatedValue = optionalDecimal(record, "estimatedValue");
        String notes = safeGet(record, "notes");

        String ticketNumber = ticketNumberGenerator.generate();

        PawnTicket ticket = PawnTicket.builder()
                .ticketNumber(ticketNumber)
                .customer(customer)
                .shop(shop)
                .loanAmount(loanAmount)
                .interestRate(interestRate)
                .interestType(interestType)
                .pawnDate(pawnDate)
                .expiryDate(expiryDate)
                .status(TicketStatus.ACTIVE)
                .notes((notes == null || notes.isBlank()) ? "Imported via CSV bulk upload" : notes)
                .build();

        String qrContent = "GOLDVAULT-TICKET:" + ticketNumber;
        ticket.setQrCode(qrCodeUtil.generateQrBase64(qrContent));

        GoldItem item = GoldItem.builder()
                .ticket(ticket)
                .description(itemDescription)
                .goldType(goldType)
                .weightGrams(weightGrams)
                .purity(purity)
                .estimatedValue(estimatedValue)
                .build();

        ticket.setGoldItems(List.of(item));

        pawnTicketRepository.save(ticket);
        return ticketNumber;
    }

    // ── Field parsing helpers ───────────────────────────────────────────────────

    private String required(CSVRecord record, String column) {
        String value = safeGet(record, column);
        if (value == null || value.isBlank()) {
            throw new RuntimeException("Missing required field: " + column);
        }
        return value;
    }

    private String safeGet(CSVRecord record, String column) {
        try {
            return record.isMapped(column) ? record.get(column) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal requiredDecimal(CSVRecord record, String column) {
        String raw = required(record, column);
        try {
            return new BigDecimal(raw.replace(",", "").trim());
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid number for " + column + ": '" + raw + "'");
        }
    }

    private BigDecimal optionalDecimal(CSVRecord record, String column) {
        String raw = safeGet(record, column);
        if (raw == null || raw.isBlank()) return null;
        try {
            return new BigDecimal(raw.replace(",", "").trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int requiredInt(CSVRecord record, String column) {
        String raw = required(record, column);
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid whole number for " + column + ": '" + raw + "'");
        }
    }

    private LocalDate parseDateOrToday(String raw) {
        if (raw == null || raw.isBlank()) return LocalDate.now();
        for (DateTimeFormatter fmt : DATE_FORMATS) {
            try {
                return LocalDate.parse(raw.trim(), fmt);
            } catch (Exception ignored) {}
        }
        throw new RuntimeException("Invalid date format for pawnDate: '" + raw + "'. Use YYYY-MM-DD.");
    }

    private InterestType parseInterestType(String raw) {
        if (raw == null || raw.isBlank()) return InterestType.FLAT;
        try {
            return InterestType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid interestType: '" + raw + "'. Use FLAT or REDUCING.");
        }
    }

    private GoldType parseGoldType(String raw) {
        if (raw == null || raw.isBlank()) return GoldType.OTHER;
        try {
            return GoldType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid goldType: '" + raw + "'. Use NECKLACE, RING, BANGLE, EARRING, CHAIN, or OTHER.");
        }
    }

    private GoldPurity parseGoldPurity(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new RuntimeException("Missing required field: purity");
        }
        String normalised = raw.trim().toUpperCase().replace("K", "K");
        try {
            return GoldPurity.valueOf(normalised);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid purity: '" + raw + "'. Use K24, K22, K21, K18, P916, P750, or OTHER.");
        }
    }
}