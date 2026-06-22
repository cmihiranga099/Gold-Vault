package lk.goldvault.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class DashboardSummaryResponse {
    private long totalShops;
    private long pendingShopApprovals;
    private long activeShops;
    private long totalCustomers;
    private long activeTickets;
    private long expiredTickets;
    private long redeemedTickets;
    private BigDecimal totalOutstandingLoans;
    private BigDecimal todayCollection;
}