package lk.goldvault.backend.service;

import lk.goldvault.backend.dto.response.ShopAnalyticsResponse;
import lk.goldvault.backend.enums.PaymentType;
import lk.goldvault.backend.enums.TicketStatus;
import lk.goldvault.backend.repository.CustomerRepository;
import lk.goldvault.backend.repository.PawnTicketRepository;
import lk.goldvault.backend.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ShopAnalyticsService {

    private final PawnTicketRepository pawnTicketRepository;
    private final PaymentRepository    paymentRepository;
    private final CustomerRepository   customerRepository;

    public ShopAnalyticsResponse getAnalytics(Long shopId) {

        LocalDate today = LocalDate.now();
        YearMonth thisMonth = YearMonth.from(today);
        YearMonth lastMonth = thisMonth.minusMonths(1);

        // ── Ticket counts ────────────────────────────────────────────────────────
        long activeTickets   = pawnTicketRepository.countByShopIdAndStatus(shopId, TicketStatus.ACTIVE);
        long expiredTickets  = pawnTicketRepository.countByShopIdAndStatus(shopId, TicketStatus.EXPIRED);
        long redeemedTickets = pawnTicketRepository.countByShopIdAndStatus(shopId, TicketStatus.REDEEMED);
        long auctionedTickets= pawnTicketRepository.countByShopIdAndStatus(shopId, TicketStatus.AUCTIONED);
        long totalTickets    = activeTickets + expiredTickets + redeemedTickets + auctionedTickets;
        long totalCustomers  = customerRepository.findByShopId(shopId).size();

        BigDecimal totalActiveLoan = pawnTicketRepository.sumActiveLoanAmountByShop(shopId);

        // ── Redemption & NPA rates ────────────────────────────────────────────────
        long closed = redeemedTickets + expiredTickets + auctionedTickets;
        double redemptionRate = closed == 0 ? 0
                : (double) redeemedTickets / closed * 100;
        double npaRate = totalTickets == 0 ? 0
                : (double) expiredTickets / totalTickets * 100;

        // ── Monthly collection ────────────────────────────────────────────────────
        BigDecimal thisMonthCollection = paymentRepository.totalCollectedByShop(
                shopId,
                thisMonth.atDay(1).atStartOfDay(),
                thisMonth.atEndOfMonth().atTime(LocalTime.MAX)
        );
        BigDecimal lastMonthCollection = paymentRepository.totalCollectedByShop(
                shopId,
                lastMonth.atDay(1).atStartOfDay(),
                lastMonth.atEndOfMonth().atTime(LocalTime.MAX)
        );

        BigDecimal momChangePct = BigDecimal.ZERO;
        if (lastMonthCollection.compareTo(BigDecimal.ZERO) > 0) {
            momChangePct = thisMonthCollection
                    .subtract(lastMonthCollection)
                    .divide(lastMonthCollection, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(1, RoundingMode.HALF_UP);
        }

        // ── Payment type breakdown this month ────────────────────────────────────
        List<Object[]> typeBreakdown = paymentRepository.collectionByTypeForShop(
                shopId,
                thisMonth.atDay(1).atStartOfDay(),
                thisMonth.atEndOfMonth().atTime(LocalTime.MAX)
        );

        BigDecimal thisMonthInterest     = BigDecimal.ZERO;
        BigDecimal thisMonthPartial      = BigDecimal.ZERO;
        BigDecimal thisMonthRedemptions  = BigDecimal.ZERO;
        BigDecimal thisMonthRenewals     = BigDecimal.ZERO;

        for (Object[] row : typeBreakdown) {
            PaymentType type = (PaymentType) row[0];
            BigDecimal  amt  = (BigDecimal)  row[1];
            switch (type) {
                case INTEREST        -> thisMonthInterest    = amt;
                case PARTIAL         -> thisMonthPartial     = amt;
                case FULL_REDEMPTION -> thisMonthRedemptions = amt;
                case RENEWAL         -> thisMonthRenewals    = amt;
            }
        }

        // ── 6-month tickets granted chart ────────────────────────────────────────
        LocalDate sixMonthsAgo = today.minusMonths(5).withDayOfMonth(1);
        List<Object[]> grantedRows = pawnTicketRepository
                .ticketsGrantedPerMonth(shopId, sixMonthsAgo);

        Map<String, Long> ticketsPerMonth = buildMonthMap6(today);
        for (Object[] row : grantedRows) {
            ticketsPerMonth.put((String) row[0], ((Number) row[1]).longValue());
        }

        // ── 6-month collection chart ─────────────────────────────────────────────
        List<Object[]> collectionRows = paymentRepository
                .monthlyCollectionByShop(shopId, sixMonthsAgo.atStartOfDay());

        Map<String, BigDecimal> collectionPerMonth = buildMonthMapBD6(today);
        for (Object[] row : collectionRows) {
            collectionPerMonth.put((String) row[0], (BigDecimal) row[1]);
        }

        // ── Expiring soon ─────────────────────────────────────────────────────────
        long expiring7  = pawnTicketRepository.findExpiringSoon(shopId, today, today.plusDays(7)).size();
        long expiring30 = pawnTicketRepository.findExpiringSoon(shopId, today, today.plusDays(30)).size();

        // ── Top customers ─────────────────────────────────────────────────────────
        List<ShopAnalyticsResponse.TopCustomer> topCustomers =
                pawnTicketRepository.topCustomersByLoanVolume(shopId)
                        .stream()
                        .limit(5)
                        .map(row -> ShopAnalyticsResponse.TopCustomer.builder()
                                .name((String) row[0])
                                .ticketCount(((Number) row[1]).longValue())
                                .totalLoanAmount((BigDecimal) row[2])
                                .build())
                        .toList();

        // ── Build response ────────────────────────────────────────────────────────
        return ShopAnalyticsResponse.builder()
                .activeTickets(activeTickets)
                .expiredTickets(expiredTickets)
                .redeemedTickets(redeemedTickets)
                .totalTicketsEver(totalTickets)
                .totalCustomers(totalCustomers)
                .totalActiveLoanAmount(totalActiveLoan)
                .thisMonthCollection(thisMonthCollection)
                .lastMonthCollection(lastMonthCollection)
                .thisMonthVsLastMonthPct(momChangePct)
                .redemptionRatePct(round2(redemptionRate))
                .npaRatePct(round2(npaRate))
                .ticketsGrantedPerMonth(ticketsPerMonth)
                .collectionPerMonth(collectionPerMonth)
                .thisMonthInterest(thisMonthInterest)
                .thisMonthPartial(thisMonthPartial)
                .thisMonthRedemptions(thisMonthRedemptions)
                .thisMonthRenewals(thisMonthRenewals)
                .expiringSoon7Days(expiring7)
                .expiringSoon30Days(expiring30)
                .topCustomers(topCustomers)
                .build();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private Map<String, Long> buildMonthMap6(LocalDate today) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (int i = 5; i >= 0; i--) {
            map.put(YearMonth.from(today.minusMonths(i)).toString(), 0L);
        }
        return map;
    }

    private Map<String, BigDecimal> buildMonthMapBD6(LocalDate today) {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        for (int i = 5; i >= 0; i--) {
            map.put(YearMonth.from(today.minusMonths(i)).toString(), BigDecimal.ZERO);
        }
        return map;
    }

    private double round2(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}