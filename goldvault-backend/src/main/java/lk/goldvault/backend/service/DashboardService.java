package lk.goldvault.backend.service;

import lk.goldvault.backend.dto.response.DashboardSummaryResponse;
import lk.goldvault.backend.entity.PawnShop;
import lk.goldvault.backend.entity.PawnTicket;
import lk.goldvault.backend.entity.Payment;
import lk.goldvault.backend.enums.ShopStatus;
import lk.goldvault.backend.enums.TicketStatus;
import lk.goldvault.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final PawnShopRepository         pawnShopRepository;
    private final CustomerRepository         customerRepository;
    private final PawnTicketRepository       pawnTicketRepository;
    private final PaymentRepository          paymentRepository;
    private final InterestCalculatorService  interestCalculatorService;
    private final AuctionRepository          auctionRepository;
    private final AuctionBidRepository       auctionBidRepository;
    private final ShopReviewRepository       shopReviewRepository;

    public DashboardSummaryResponse getSummary() {
        LocalDate today = LocalDate.now();

        // ── Existing core stats ──────────────────────────────────────────────────
        long totalShops     = pawnShopRepository.count();
        long pendingShops   = pawnShopRepository.findByStatus(ShopStatus.PENDING).size();
        long activeShopsCnt = pawnShopRepository.findByStatus(ShopStatus.ACTIVE).size();
        long suspendedShops = pawnShopRepository.findByStatus(ShopStatus.SUSPENDED).size();
        long totalCustomers = customerRepository.count();

        List<PawnTicket> allTickets = pawnTicketRepository.findAll();
        long activeTickets   = allTickets.stream().filter(t -> t.getStatus() == TicketStatus.ACTIVE).count();
        long expiredTickets  = allTickets.stream().filter(t -> t.getStatus() == TicketStatus.EXPIRED).count();
        long redeemedTickets = allTickets.stream().filter(t -> t.getStatus() == TicketStatus.REDEEMED).count();
        long auctionedTickets= allTickets.stream().filter(t -> t.getStatus() == TicketStatus.AUCTIONED).count();

        BigDecimal totalOutstanding = allTickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.ACTIVE || t.getStatus() == TicketStatus.EXPIRED)
                .map(t -> interestCalculatorService.calculateOutstandingBalance(t, today))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDateTime startOfToday = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime endOfToday   = LocalDateTime.of(today, LocalTime.MAX);
        BigDecimal todayCollection = paymentRepository
                .findByPaymentDateBetween(startOfToday, endOfToday)
                .stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        // ── New: NPA + redemption rate ───────────────────────────────────────────
        long closedTickets = redeemedTickets + expiredTickets + auctionedTickets;
        double npaRate = allTickets.isEmpty() ? 0
                : (double) expiredTickets / allTickets.size() * 100;
        double redemptionRate = closedTickets == 0 ? 0
                : (double) redeemedTickets / closedTickets * 100;

        // ── New: auction stats ───────────────────────────────────────────────────
        long openAuctions = auctionRepository.findByStatusOrderByEndsAtAsc("OPEN").size();
        BigDecimal totalBidVolume = auctionBidRepository.findAll().stream()
                .map(b -> b.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ── New: review stats ────────────────────────────────────────────────────
        long totalReviews = shopReviewRepository.findAll().size();
        double avgRating = shopReviewRepository.findAll().stream()
                .filter(r -> "VISIBLE".equals(r.getStatus()))
                .mapToInt(r -> r.getRating())
                .average()
                .orElse(0.0);

        // ── New: shops added this month ──────────────────────────────────────────
        YearMonth currentMonth = YearMonth.from(today);
        long shopsThisMonth = pawnShopRepository.findAll().stream()
                .filter(s -> s.getCreatedAt() != null
                        && YearMonth.from(s.getCreatedAt().toLocalDate()).equals(currentMonth))
                .count();

        // ── New: 6-month collection + new ticket trend ───────────────────────────
        Map<String, BigDecimal> collectionTrend = buildEmptyMonthMapBD(today);
        Map<String, Long> ticketTrend = buildEmptyMonthMapLong(today);

        LocalDate sixMonthsAgo = today.minusMonths(5).withDayOfMonth(1);

        List<Payment> recentPayments = paymentRepository.findAll().stream()
                .filter(p -> p.getPaymentDate() != null && !p.getPaymentDate().toLocalDate().isBefore(sixMonthsAgo))
                .toList();
        for (Payment p : recentPayments) {
            String key = YearMonth.from(p.getPaymentDate()).toString();
            collectionTrend.merge(key, p.getAmount(), BigDecimal::add);
        }

        List<PawnTicket> recentTickets = allTickets.stream()
                .filter(t -> t.getPawnDate() != null && !t.getPawnDate().isBefore(sixMonthsAgo))
                .toList();
        for (PawnTicket t : recentTickets) {
            String key = YearMonth.from(t.getPawnDate()).toString();
            ticketTrend.merge(key, 1L, Long::sum);
        }

        // ── New: top shops by loan volume this month ─────────────────────────────
        LocalDate monthStart = currentMonth.atDay(1);
        Map<Long, List<PawnTicket>> ticketsByShop = allTickets.stream()
                .filter(t -> t.getPawnDate() != null && !t.getPawnDate().isBefore(monthStart))
                .collect(Collectors.groupingBy(t -> t.getShop().getId()));

        List<DashboardSummaryResponse.ShopLeaderboardEntry> topShops = ticketsByShop.entrySet().stream()
                .map(e -> {
                    PawnShop shop = e.getValue().get(0).getShop();
                    BigDecimal volume = e.getValue().stream()
                            .map(PawnTicket::getLoanAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return DashboardSummaryResponse.ShopLeaderboardEntry.builder()
                            .shopId(shop.getId())
                            .shopName(shop.getName())
                            .volume(volume)
                            .ticketCount(e.getValue().size())
                            .build();
                })
                .sorted((a, b) -> b.getVolume().compareTo(a.getVolume()))
                .limit(5)
                .toList();

        // ── New: inactive shops (no new ticket in 30 days) — churn risk ──────────
        LocalDate thirtyDaysAgo = today.minusDays(30);
        Set<Long> activeShopIds = allTickets.stream()
                .filter(t -> t.getPawnDate() != null && !t.getPawnDate().isBefore(thirtyDaysAgo))
                .map(t -> t.getShop().getId())
                .collect(Collectors.toSet());

        List<DashboardSummaryResponse.ShopLeaderboardEntry> inactiveShops = pawnShopRepository
                .findByStatus(ShopStatus.ACTIVE).stream()
                .filter(s -> !activeShopIds.contains(s.getId()))
                .map(s -> DashboardSummaryResponse.ShopLeaderboardEntry.builder()
                        .shopId(s.getId())
                        .shopName(s.getName())
                        .volume(BigDecimal.ZERO)
                        .ticketCount(0)
                        .build())
                .limit(10)
                .toList();

        return DashboardSummaryResponse.builder()
                .totalShops(totalShops)
                .pendingShopApprovals(pendingShops)
                .activeShops(activeShopsCnt)
                .totalCustomers(totalCustomers)
                .activeTickets(activeTickets)
                .expiredTickets(expiredTickets)
                .redeemedTickets(redeemedTickets)
                .totalOutstandingLoans(totalOutstanding)
                .todayCollection(todayCollection)
                .auctionedTickets(auctionedTickets)
                .npaRatePercent(round2(npaRate))
                .redemptionRatePercent(round2(redemptionRate))
                .openAuctions(openAuctions)
                .totalAuctionBidVolume(totalBidVolume)
                .totalReviews(totalReviews)
                .platformAverageRating(round2(avgRating))
                .suspendedShops(suspendedShops)
                .shopsAddedThisMonth(shopsThisMonth)
                .collectionTrendLast6Months(collectionTrend)
                .newTicketsTrendLast6Months(ticketTrend)
                .topShopsByVolume(topShops)
                .inactiveShopsLast30Days(inactiveShops)
                .build();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private Map<String, BigDecimal> buildEmptyMonthMapBD(LocalDate today) {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        for (int i = 5; i >= 0; i--) map.put(YearMonth.from(today.minusMonths(i)).toString(), BigDecimal.ZERO);
        return map;
    }

    private Map<String, Long> buildEmptyMonthMapLong(LocalDate today) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (int i = 5; i >= 0; i--) map.put(YearMonth.from(today.minusMonths(i)).toString(), 0L);
        return map;
    }

    private double round2(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}