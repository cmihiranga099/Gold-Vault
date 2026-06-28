package lk.goldvault.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
public class ShopRatingResponse {
    private Long            shopId;
    private String          shopName;
    private double          averageRating;
    private long            totalReviews;
    /** e.g. { 5: 10, 4: 5, 3: 2, 2: 1, 1: 0 } */
    private Map<Integer, Long> ratingDistribution;
}