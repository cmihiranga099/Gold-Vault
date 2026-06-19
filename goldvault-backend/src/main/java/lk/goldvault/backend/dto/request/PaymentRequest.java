package lk.goldvault.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lk.goldvault.backend.enums.PaymentMethod;
import lk.goldvault.backend.enums.PaymentType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentRequest {

    @NotNull(message = "Ticket id is required")
    private Long ticketId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Payment type is required")
    private PaymentType paymentType;

    private PaymentMethod paymentMethod;

    private String referenceNumber;

    private Long receivedBy;
}