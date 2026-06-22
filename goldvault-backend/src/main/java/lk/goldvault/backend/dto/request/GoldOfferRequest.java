package lk.goldvault.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class GoldOfferRequest {

    @NotNull(message = "Listing id is required")
    private Long listingId;

    @NotNull(message = "Offer price is required")
    @DecimalMin(value = "0.01", message = "Offer price must be greater than 0")
    private BigDecimal offerPrice;

    private String message;
}