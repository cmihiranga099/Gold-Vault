package lk.goldvault.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class PromotionResponse {
    private Long          id;
    private Long          shopId;
    private String        shopName;
    private String        title;
    private String        description;
    private String        promoType;
    private BigDecimal    promoValue;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private String        status;
    private boolean       currentlyActive;
    private long          daysRemaining;
}