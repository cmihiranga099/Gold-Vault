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
public class ShopProfileResponse {

    // ── Shop details ──────────────────────────────────────────────────────────
    private Long    id;
    private String  name;
    private String  ownerName;
    private String  phone;
    private String  email;
    private String  address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String  status;

    // ── Gold buying rates (purity → rate per gram) ────────────────────────────
    private Map<String, BigDecimal> goldRates;

    // ── Ratings ───────────────────────────────────────────────────────────────
    private double averageRating;
    private long   totalReviews;
    private Map<Integer, Long> ratingDistribution; // 5→count, 4→count...

    // ── Recent visible reviews ────────────────────────────────────────────────
    private List<ReviewResponse> recentReviews;

    // ── Active promotions ─────────────────────────────────────────────────────
    private List<PromotionResponse> activePromotions;

    // ── Platform stats ────────────────────────────────────────────────────────
    private long totalTicketsEver;
    private long activeTickets;
}