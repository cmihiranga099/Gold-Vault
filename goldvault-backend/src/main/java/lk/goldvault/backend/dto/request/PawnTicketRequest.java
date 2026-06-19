package lk.goldvault.backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lk.goldvault.backend.enums.InterestType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class PawnTicketRequest {

    @NotNull(message = "Customer id is required")
    private Long customerId;

    private Long branchId;

    @NotNull(message = "Loan amount is required")
    @DecimalMin(value = "1", message = "Loan amount must be greater than 0")
    private BigDecimal loanAmount;

    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0", message = "Interest rate cannot be negative")
    private BigDecimal interestRate;

    private InterestType interestType;

    /** Loan period in months — used to calculate expiryDate from pawnDate */
    @NotNull(message = "Loan period (months) is required")
    private Integer periodMonths;

    private String notes;

    @NotEmpty(message = "At least one gold item is required")
    @Valid
    private List<GoldItemRequest> goldItems;
}