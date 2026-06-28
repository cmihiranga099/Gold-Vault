package lk.goldvault.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class ShopAnalyticsResponse {

    // ── Overview KPIs ────────────────────────────────────────────────────────────
    private long    activeTickets;
    private long    expiredTickets;
    private long    redeemedTickets;
    private long    totalTicketsEver;
    private long    totalCustomers;
    private BigDecimal totalActiveLoanAmount;

    // ── Current month ────────────────────────────────────────────────────────────
    private BigDecimal thisMonthCollection;
    private BigDecimal lastMonthCollection;
    private BigDecimal thisMonthVsLastMonthPct;   // +/- % change

    // ── Redemption rate ──────────────────────────────────────────────────────────
    private double redemptionRatePct;   // redeemed / (redeemed + expired + auctioned) * 100
    private double npaRatePct;          // expired / total * 100  (non-performing)

    // ── Charts data (last 6 months) ──────────────────────────────────────────────
    /** e.g. { "2026-01": 12, "2026-02": 8, ... } */
    private Map<String, Long>       ticketsGrantedPerMonth;
    /** e.g. { "2026-01": 125000.00, "2026-02": 98000.00, ... } */
    private Map<String, BigDecimal> collectionPerMonth;

    // ── Payment breakdown (this month) ──────────────────────────────────────────
    private BigDecimal thisMonthInterest;
    private BigDecimal thisMonthPartial;
    private BigDecimal thisMonthRedemptions;
    private BigDecimal thisMonthRenewals;

    // ── Expiring soon ────────────────────────────────────────────────────────────
    private long expiringSoon7Days;
    private long expiringSoon30Days;

    // ── Top customers ────────────────────────────────────────────────────────────
    private List<TopCustomer> topCustomers;

    @Getter
    @Setter
    @Builder
    public static class TopCustomer {
        private String     name;
        private long       ticketCount;
        private BigDecimal totalLoanAmount;
    }
}