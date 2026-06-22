package lk.goldvault.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
public class RevenueReportResponse {
    private LocalDate startDate;
    private LocalDate endDate;

    /** Total payment volume processed across all shops in the period */
    private BigDecimal totalPaymentVolume;

    /** Platform commission earned on that volume */
    private BigDecimal totalCommission;

    private long totalPaymentCount;
    private long activeShopCount;
    private long activeTicketCount;

    private List<ShopRevenueBreakdown> byShop;

    @Getter
    @Setter
    @Builder
    public static class ShopRevenueBreakdown {
        private Long shopId;
        private String shopName;
        private BigDecimal paymentVolume;
        private BigDecimal commissionOwed;
        private long paymentCount;
    }
}