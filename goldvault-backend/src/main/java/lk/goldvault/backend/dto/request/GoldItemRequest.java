package lk.goldvault.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lk.goldvault.backend.enums.GoldPurity;
import lk.goldvault.backend.enums.GoldType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class GoldItemRequest {

    @NotBlank(message = "Item description is required")
    private String description;

    @NotNull(message = "Gold type is required")
    private GoldType goldType;

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.001", message = "Weight must be greater than 0")
    private BigDecimal weightGrams;

    @NotNull(message = "Purity is required")
    private GoldPurity purity;

    private BigDecimal estimatedValue;

    private String photoUrl;
}