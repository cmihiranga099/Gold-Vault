package lk.goldvault.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class LoyaltySummaryResponse {
    private int currentPoints;
    /** How much discount 1 point is worth, e.g. 0.01 = 1 point reduces interest rate by 0.01% */
    private double pointValuePercent;
    private int pointsToNextReward;
    private List<LoyaltyTransactionResponse> history;
}