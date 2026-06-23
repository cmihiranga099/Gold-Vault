package lk.goldvault.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class ShopDashboardResponse {
    private long totalCustomers;
    private long activeTickets;
    private long expiringSoonCount;
    private long expiredTickets;
    private BigDecimal totalOutstandingLoans;
    private BigDecimal todayCollection;
    private long todayPaymentCount;
}