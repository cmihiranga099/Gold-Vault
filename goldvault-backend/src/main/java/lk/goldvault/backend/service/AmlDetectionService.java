package lk.goldvault.backend.service;

import lk.goldvault.backend.dto.request.AmlReviewRequest;
import lk.goldvault.backend.dto.response.AmlFlagResponse;
import lk.goldvault.backend.entity.AmlFlag;
import lk.goldvault.backend.entity.Customer;
import lk.goldvault.backend.entity.PawnTicket;
import lk.goldvault.backend.enums.TicketStatus;
import lk.goldvault.backend.repository.AmlFlagRepository;
import lk.goldvault.backend.repository.CustomerRepository;
import lk.goldvault.backend.repository.PawnTicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AmlDetectionService {

    private final AmlFlagRepository    amlFlagRepository;
    private final CustomerRepository   customerRepository;
    private final PawnTicketRepository pawnTicketRepository;

    // ── Thresholds (CBSL AML guidelines for pawn brokers) ───────────────────
    private static final BigDecimal LARGE_TRANSACTION_THRESHOLD = new BigDecimal("500000");   // LKR 500k single ticket
    private static final BigDecimal HIGH_VOLUME_THRESHOLD       = new BigDecimal("1000000");  // LKR 1M in 30 days
    private static final int        RAPID_CYCLING_COUNT         = 3;    // ≥3 pawn+redeem cycles in 90 days
    private static final int        MULTIPLE_SHOPS_COUNT        = 3;    // ≥3 different shops in 30 days

    // ── Scheduled daily scan ─────────────────────────────────────────────────

    @Scheduled(cron = "0 0 7 * * *", zone = "Asia/Colombo")
    @Transactional
    public void runDailyAmlScan() {
        log.info("[AML] Starting daily AML scan...");
        int flags = 0;
        flags += scanLargeTransactions();
        flags += scanHighVolume();
        flags += scanMultipleShops();
        log.info("[AML] Daily scan complete. {} new flag(s) raised.", flags);
    }

    // ── Manual trigger (admin) ────────────────────────────────────────────────

    @Transactional
    public int triggerScan() {
        int flags = 0;
        flags += scanLargeTransactions();
        flags += scanHighVolume();
        flags += scanMultipleShops();
        return flags;
    }

    // ── Rule 1: Large transaction ─────────────────────────────────────────────

    private int scanLargeTransactions() {
        LocalDate since = LocalDate.now().minusDays(1); // only check yesterday's new tickets
        List<PawnTicket> recent = pawnTicketRepository.findAll().stream()
                .filter(t -> t.getPawnDate() != null && !t.getPawnDate().isBefore(since))
                .filter(t -> t.getLoanAmount().compareTo(LARGE_TRANSACTION_THRESHOLD) >= 0)
                .toList();

        int count = 0;
        for (PawnTicket ticket : recent) {
            boolean exists = amlFlagRepository
                    .existsByCustomerIdAndFlagTypeAndTicketIdAndCreatedAtAfter(
                            ticket.getCustomer().getId(), "LARGE_TRANSACTION",
                            ticket.getId(), LocalDateTime.now().minusDays(2));
            if (exists) continue;

            raiseFlag(
                ticket.getCustomer(), ticket.getShop(), ticket,
                "LARGE_TRANSACTION",
                String.format("Loan of LKR %,.0f on ticket %s exceeds CBSL reporting threshold of LKR 500,000.",
                        ticket.getLoanAmount(), ticket.getTicketNumber()),
                ticket.getLoanAmount()
            );
            count++;
        }
        return count;
    }

    // ── Rule 2: High volume in 30 days ────────────────────────────────────────

    private int scanHighVolume() {
        LocalDate from = LocalDate.now().minusDays(30);
        List<PawnTicket> recentTickets = pawnTicketRepository.findAll().stream()
                .filter(t -> t.getPawnDate() != null && !t.getPawnDate().isBefore(from))
                .toList();

        Map<Long, List<PawnTicket>> byCustomer = recentTickets.stream()
                .collect(Collectors.groupingBy(t -> t.getCustomer().getId()));

        int count = 0;
        for (Map.Entry<Long, List<PawnTicket>> entry : byCustomer.entrySet()) {
            BigDecimal total = entry.getValue().stream()
                    .map(PawnTicket::getLoanAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (total.compareTo(HIGH_VOLUME_THRESHOLD) < 0) continue;

            PawnTicket last = entry.getValue().get(entry.getValue().size() - 1);
            boolean exists = amlFlagRepository
                    .existsByCustomerIdAndFlagTypeAndTicketIdAndCreatedAtAfter(
                            entry.getKey(), "HIGH_VOLUME", null,
                            LocalDateTime.now().minusDays(1));
            if (exists) continue;

            raiseFlag(
                last.getCustomer(), last.getShop(), null,
                "HIGH_VOLUME",
                String.format("Customer %s pawned LKR %,.0f across %d ticket(s) in the last 30 days. Exceeds CBSL threshold of LKR 1,000,000.",
                        last.getCustomer().getFullName(), total, entry.getValue().size()),
                total
            );
            count++;
        }
        return count;
    }

    // ── Rule 3: Multiple shops in 30 days ────────────────────────────────────

    private int scanMultipleShops() {
        LocalDate from = LocalDate.now().minusDays(30);
        List<PawnTicket> recent = pawnTicketRepository.findAll().stream()
                .filter(t -> t.getPawnDate() != null && !t.getPawnDate().isBefore(from))
                .toList();

        Map<Long, List<PawnTicket>> byCustomer = recent.stream()
                .collect(Collectors.groupingBy(t -> t.getCustomer().getId()));

        int count = 0;
        for (Map.Entry<Long, List<PawnTicket>> entry : byCustomer.entrySet()) {
            Set<Long> shopIds = entry.getValue().stream()
                    .map(t -> t.getShop().getId())
                    .collect(Collectors.toSet());

            if (shopIds.size() < MULTIPLE_SHOPS_COUNT) continue;

            PawnTicket any = entry.getValue().get(0);
            boolean exists = amlFlagRepository
                    .existsByCustomerIdAndFlagTypeAndTicketIdAndCreatedAtAfter(
                            entry.getKey(), "MULTIPLE_SHOPS", null,
                            LocalDateTime.now().minusDays(1));
            if (exists) continue;

            raiseFlag(
                any.getCustomer(), any.getShop(), null,
                "MULTIPLE_SHOPS",
                String.format("Customer %s has used %d different pawn shops in the last 30 days. This pattern may indicate AML structuring.",
                        any.getCustomer().getFullName(), shopIds.size()),
                null
            );
            count++;
        }
        return count;
    }

    // ── Admin: review/dismiss a flag ──────────────────────────────────────────

    @Transactional
    public AmlFlagResponse reviewFlag(Long flagId, AmlReviewRequest request) {
        AmlFlag flag = amlFlagRepository.findById(flagId)
                .orElseThrow(() -> new RuntimeException("AML flag not found: " + flagId));

        flag.setStatus(request.getStatus().toUpperCase());
        flag.setReviewedBy(request.getReviewedBy());
        flag.setReviewedAt(LocalDateTime.now());
        flag.setReviewNote(request.getReviewNote());

        return toResponse(amlFlagRepository.save(flag));
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    public List<AmlFlagResponse> getOpenFlags() {
        return amlFlagRepository.findByStatusOrderByCreatedAtDesc("OPEN")
                .stream().map(this::toResponse).toList();
    }

    public List<AmlFlagResponse> getAllFlags() {
        return amlFlagRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toResponse).toList();
    }

    public List<AmlFlagResponse> getFlagsByCustomer(Long customerId) {
        return amlFlagRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream().map(this::toResponse).toList();
    }

    public AmlSummary getSummary() {
        long open      = amlFlagRepository.countByStatus("OPEN");
        long reviewed  = amlFlagRepository.countByStatus("REVIEWED");
        long dismissed = amlFlagRepository.countByStatus("DISMISSED");

        Map<String, Long> byType = amlFlagRepository.countByFlagType()
                .stream().collect(Collectors.toMap(
                        r -> (String)  r[0],
                        r -> (Long)    r[1]
                ));

        return new AmlSummary(open, reviewed, dismissed, byType);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private void raiseFlag(Customer customer, lk.goldvault.backend.entity.PawnShop shop,
                           PawnTicket ticket, String type, String desc, BigDecimal amount) {
        AmlFlag flag = AmlFlag.builder()
                .customer(customer)
                .shop(shop)
                .ticket(ticket)
                .flagType(type)
                .description(desc)
                .amount(amount)
                .status("OPEN")
                .build();
        amlFlagRepository.save(flag);
        log.warn("[AML] {} flag raised for customer {} ({}): {}",
                type, customer.getId(), customer.getNic(), desc);
    }

    private String flagTypeLabel(String type) {
        return switch (type) {
            case "LARGE_TRANSACTION" -> "Large transaction (≥ LKR 500K)";
            case "HIGH_VOLUME"       -> "High volume (≥ LKR 1M / 30 days)";
            case "RAPID_CYCLING"     -> "Rapid cycling (≥ 3 pawn/redeem cycles)";
            case "MULTIPLE_SHOPS"    -> "Multiple shops (≥ 3 shops / 30 days)";
            default -> type;
        };
    }

    private AmlFlagResponse toResponse(AmlFlag f) {
        return AmlFlagResponse.builder()
                .id(f.getId())
                .customerId(f.getCustomer().getId())
                .customerName(f.getCustomer().getFullName())
                .customerNic(f.getCustomer().getNic())
                .shopId(f.getShop().getId())
                .shopName(f.getShop().getName())
                .ticketId(f.getTicket() != null ? f.getTicket().getId() : null)
                .ticketNumber(f.getTicket() != null ? f.getTicket().getTicketNumber() : null)
                .flagType(f.getFlagType())
                .flagTypeLabel(flagTypeLabel(f.getFlagType()))
                .description(f.getDescription())
                .amount(f.getAmount())
                .status(f.getStatus())
                .reviewedBy(f.getReviewedBy())
                .reviewedAt(f.getReviewedAt())
                .reviewNote(f.getReviewNote())
                .createdAt(f.getCreatedAt())
                .build();
    }

    // ── Inner record ──────────────────────────────────────────────────────────

    public record AmlSummary(
            long openFlags, long reviewedFlags, long dismissedFlags,
            Map<String, Long> byType) {}
}