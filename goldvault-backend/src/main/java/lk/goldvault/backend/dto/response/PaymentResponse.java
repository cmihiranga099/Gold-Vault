package lk.goldvault.backend.dto.response;

import lk.goldvault.backend.enums.PaymentMethod;
import lk.goldvault.backend.enums.PaymentType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class PaymentResponse {
    private Long id;
    private Long ticketId;
    private String ticketNumber;
    private BigDecimal amount;
    private PaymentType paymentType;
    private PaymentMethod paymentMethod;
    private String referenceNumber;
    private LocalDateTime paymentDate;
    private Long receivedBy;

    /** Ticket's outstanding balance immediately after this payment is applied */
    private BigDecimal remainingBalance;

    /** Whether this payment caused the ticket to become fully REDEEMED */
    private boolean ticketRedeemed;
}