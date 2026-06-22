package lk.goldvault.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lk.goldvault.backend.enums.GoldPurity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class GoldRateRequest {

    @NotNull(message = "Purity is required")
    private GoldPurity purity;

    @NotNull(message = "Rate per gram is required")
    @DecimalMin(value = "0.01", message = "Rate must be greater than 0")
    private BigDecimal ratePerGram;
}