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
public class DashboardSummaryResponse {
    // ── Existing fields (unchanged) ──────────────────────────────────────────────
    private long totalShops;
    private long pendingShopApprovals;
    private long activeShops;
    private long totalCustomers;
    private long activeTickets;
    private long expiredTickets;
    private long redeemedTickets;
    private BigDecimal totalOutstandingLoans;
    private BigDecimal todayCollection;

    // ── New: super-dashboard KPIs ────────────────────────────────────────────────
    private long auctionedTickets;
    private double npaRatePercent;          // expired / total tickets * 100
    private double redemptionRatePercent;   // redeemed / (redeemed+expired+auctioned) * 100

    private long openAuctions;
    private BigDecimal totalAuctionBidVolume;

    private long totalReviews;
    private double platformAverageRating;

    private long suspendedShops;
    private long shopsAddedThisMonth;

    /** GMV (marketplace + pawn loan volume) trend for the last 6 months, key = "YYYY-MM" */
    private Map<String, BigDecimal> collectionTrendLast6Months;
    private Map<String, Long>       newTicketsTrendLast6Months;

    /** Top 5 shops ranked by total loan volume this month */
    private List<ShopLeaderboardEntry> topShopsByVolume;

    /** Shops with no activity (no new tickets) in the last 30 days — churn risk */
    private List<ShopLeaderboardEntry> inactiveShopsLast30Days;

    @Getter
    @Setter
    @Builder
    public static class ShopLeaderboardEntry {
        private Long shopId;
        private String shopName;
        private BigDecimal volume;
        private long ticketCount;
    }
}