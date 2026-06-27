package lk.goldvault.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lk.goldvault.backend.enums.PaymentMethod;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RenewalRequest {

    /** How many months to extend the expiry date by. Min 1. */
    @NotNull(message = "Extension months is required")
    @Min(value = 1, message = "Must extend by at least 1 month")
    private Integer extensionMonths;

    /** Interest payment made at time of renewal (must cover accrued interest). */
    @NotNull(message = "Interest payment amount is required")
    @DecimalMin(value = "0.01", message = "Interest payment must be greater than 0")
    private BigDecimal interestPaid;

    /** Payment method used for the interest payment. */
    private PaymentMethod paymentMethod;

    /** Optional bank/transfer reference number. */
    private String referenceNumber;
}