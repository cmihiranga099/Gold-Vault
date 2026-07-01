package lk.goldvault.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class MarketRateRequest {

    @NotBlank(message = "Purity is required")
    private String purity;

    @NotNull(message = "Rate per gram is required")
    @DecimalMin(value = "0.01", message = "Rate must be greater than 0")
    private BigDecimal ratePerGram;

    private LocalDate effectiveDate; // defaults to today if null
}