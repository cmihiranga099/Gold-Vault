package lk.goldvault.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PlaceBidRequest {

    @NotBlank(message = "Bidder name is required")
    private String bidderName;

    @NotBlank(message = "Bidder phone is required")
    private String bidderPhone;

    @NotNull(message = "Bid amount is required")
    @DecimalMin(value = "0.01", message = "Bid amount must be greater than 0")
    private BigDecimal amount;
}