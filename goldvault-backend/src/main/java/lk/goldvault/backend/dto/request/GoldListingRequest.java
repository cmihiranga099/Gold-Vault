package lk.goldvault.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lk.goldvault.backend.enums.GoldPurity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class GoldListingRequest {

    private String description;

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.001", message = "Weight must be greater than 0")
    private BigDecimal weightGrams;

    @NotNull(message = "Purity is required")
    private GoldPurity purity;

    private BigDecimal askingPrice;
}