package lk.goldvault.backend.dto.response;

import lk.goldvault.backend.enums.GoldPurity;
import lk.goldvault.backend.enums.ListingStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class GoldListingResponse {
    private Long id;
    private Long customerId;
    private String customerName;
    private String description;
    private BigDecimal weightGrams;
    private GoldPurity purity;
    private BigDecimal askingPrice;
    private ListingStatus status;
    private LocalDateTime createdAt;
    private List<GoldOfferResponse> offers;

    /** Best (highest) offer price currently on this listing, if any */
    private BigDecimal bestOfferPrice;
}