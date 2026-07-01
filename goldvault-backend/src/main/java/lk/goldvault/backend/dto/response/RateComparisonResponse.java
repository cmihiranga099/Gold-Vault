package lk.goldvault.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
public class RateComparisonResponse {
    private String     purity;
    private BigDecimal marketRate;      // platform reference rate
    private List<ShopRateEntry> shopRates;

    @Getter
    @Setter
    @Builder
    public static class ShopRateEntry {
        private Long       shopId;
        private String     shopName;
        private BigDecimal ratePerGram;
        private boolean    aboveMarket;   // true if shop rate > market rate
        private double     diffPercent;   // how far from market rate in %
    }
}