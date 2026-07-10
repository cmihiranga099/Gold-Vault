package lk.goldvault.backend.dto.response;

import lk.goldvault.backend.enums.PaymentType;
import lk.goldvault.backend.enums.SubmissionStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class PaymentSubmissionResponse {
    private Long id;
    private Long ticketId;
    private String ticketNumber;
    private String shopName;
    private Long customerId;
    private String customerName;
    private BigDecimal amount;
    private PaymentType paymentType;
    private String bankName;
    private String referenceNumber;
    private String receiptUrl;
    private SubmissionStatus status;
    private String rejectionReason;
    private Long resultingPaymentId;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
}