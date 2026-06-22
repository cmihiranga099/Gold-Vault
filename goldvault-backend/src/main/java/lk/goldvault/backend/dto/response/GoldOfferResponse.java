package lk.goldvault.backend.dto.response;

import lk.goldvault.backend.enums.OfferStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class GoldOfferResponse {
    private Long id;
    private Long listingId;
    private Long shopId;
    private String shopName;
    private BigDecimal offerPrice;
    private String message;
    private OfferStatus status;
    private LocalDateTime createdAt;
}