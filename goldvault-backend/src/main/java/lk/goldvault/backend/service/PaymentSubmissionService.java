package lk.goldvault.backend.service;

import lk.goldvault.backend.dto.request.PaymentRequest;
import lk.goldvault.backend.dto.response.PaymentResponse;
import lk.goldvault.backend.dto.response.PaymentSubmissionResponse;
import lk.goldvault.backend.entity.Customer;
import lk.goldvault.backend.entity.Notification;
import lk.goldvault.backend.entity.PawnTicket;
import lk.goldvault.backend.entity.PaymentSubmission;
import lk.goldvault.backend.enums.*;
import lk.goldvault.backend.repository.NotificationRepository;
import lk.goldvault.backend.repository.PawnTicketRepository;
import lk.goldvault.backend.repository.PaymentSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Online repayment: a customer submits a bank-transfer payment with an
 * uploaded receipt instead of visiting the shop. Shop staff review it and
 * either approve — which books a real {@link lk.goldvault.backend.entity.Payment}
 * through the existing {@link PaymentService} exactly as a cash payment would
 * be — or reject it with a reason. Either outcome notifies the customer
 * through the existing notification bell.
 */
@Service
@RequiredArgsConstructor
public class PaymentSubmissionService {

    /** Allow a small buffer over the exact outstanding balance (rounding, a slightly
     *  early top-up, etc.) before flagging the submission as implausible. Anything
     *  bigger than this is almost certainly a mistaken amount — better to tell the
     *  customer immediately than let it sit in the shop's review queue. */
    private static final BigDecimal OVER_BALANCE_TOLERANCE = new BigDecimal("1.05");

    private final PaymentSubmissionRepository paymentSubmissionRepository;
    private final PawnTicketRepository pawnTicketRepository;
    private final PaymentService paymentService;
    private final InterestCalculatorService interestCalculatorService;
    private final FileUploadService fileUploadService;
    private final NotificationRepository notificationRepository;

    // ── Customer: submit ─────────────────────────────────────────────────────────

    @Transactional
    public PaymentSubmissionResponse submit(Long customerId, Long ticketId, BigDecimal amount,
                                             PaymentType paymentType, String bankName,
                                             String referenceNumber, MultipartFile receipt) {
        PawnTicket ticket = pawnTicketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        if (!ticket.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("This ticket does not belong to this customer.");
        }
        if (ticket.getStatus() != TicketStatus.ACTIVE && ticket.getStatus() != TicketStatus.EXPIRED) {
            throw new RuntimeException(
                    "Cannot submit a payment for a ticket with status: " + ticket.getStatus());
        }

        BigDecimal balance = interestCalculatorService.calculateOutstandingBalance(ticket, LocalDate.now());
        if (paymentType != PaymentType.FULL_REDEMPTION
                && amount.compareTo(balance.multiply(OVER_BALANCE_TOLERANCE)) > 0) {
            throw new RuntimeException(
                    "Amount (" + amount + ") is well above the outstanding balance (" + balance + ").");
        }

        String receiptUrl = fileUploadService.save(receipt, "payment-receipts");

        PaymentSubmission submission = PaymentSubmission.builder()
                .ticket(ticket)
                .customer(ticket.getCustomer())
                .amount(amount)
                .paymentType(paymentType)
                .bankName(bankName)
                .referenceNumber(referenceNumber)
                .receiptUrl(receiptUrl)
                .status(SubmissionStatus.PENDING)
                .build();

        return toResponse(paymentSubmissionRepository.save(submission));
    }

    public List<PaymentSubmissionResponse> getForTicket(Long ticketId) {
        return paymentSubmissionRepository.findByTicketIdOrderBySubmittedAtDesc(ticketId)
                .stream().map(this::toResponse).toList();
    }

    public List<PaymentSubmissionResponse> getForCustomer(Long customerId) {
        return paymentSubmissionRepository.findByCustomerIdOrderBySubmittedAtDesc(customerId)
                .stream().map(this::toResponse).toList();
    }

    // ── Shop: review ─────────────────────────────────────────────────────────────

    public List<PaymentSubmissionResponse> getPendingForShop(Long shopId) {
        return paymentSubmissionRepository
                .findByStatusAndTicket_Shop_IdOrderBySubmittedAtAsc(SubmissionStatus.PENDING, shopId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public PaymentSubmissionResponse approve(Long submissionId, Long reviewedBy) {
        PaymentSubmission submission = requirePending(submissionId);

        PaymentRequest request = new PaymentRequest();
        request.setTicketId(submission.getTicket().getId());
        request.setAmount(submission.getAmount());
        request.setPaymentType(submission.getPaymentType());
        request.setPaymentMethod(PaymentMethod.ONLINE_TRANSFER);
        request.setReferenceNumber(submission.getReferenceNumber());
        request.setReceivedBy(reviewedBy);

        PaymentResponse payment = paymentService.recordPayment(request);

        submission.setStatus(SubmissionStatus.APPROVED);
        submission.setResultingPaymentId(payment.getId());
        submission.setReviewedBy(reviewedBy);
        submission.setReviewedAt(LocalDateTime.now());
        paymentSubmissionRepository.save(submission);

        notifyCustomer(submission,
                (payment.isTicketRedeemed()
                        ? "Your online payment of LKR " + submission.getAmount()
                        + " for ticket " + submission.getTicket().getTicketNumber()
                        + " has been confirmed — the ticket is now fully redeemed."
                        : "Your online payment of LKR " + submission.getAmount()
                        + " for ticket " + submission.getTicket().getTicketNumber()
                        + " has been confirmed."));

        return toResponse(submission);
    }

    @Transactional
    public PaymentSubmissionResponse reject(Long submissionId, Long reviewedBy, String reason) {
        PaymentSubmission submission = requirePending(submissionId);

        submission.setStatus(SubmissionStatus.REJECTED);
        submission.setRejectionReason(reason);
        submission.setReviewedBy(reviewedBy);
        submission.setReviewedAt(LocalDateTime.now());
        paymentSubmissionRepository.save(submission);

        notifyCustomer(submission,
                "Your online payment of LKR " + submission.getAmount()
                        + " for ticket " + submission.getTicket().getTicketNumber()
                        + " could not be confirmed: " + reason
                        + ". Please check the details or visit the shop.");

        return toResponse(submission);
    }

    private PaymentSubmission requirePending(Long submissionId) {
        PaymentSubmission submission = paymentSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Payment submission not found: " + submissionId));
        if (submission.getStatus() != SubmissionStatus.PENDING) {
            throw new RuntimeException("This submission has already been " + submission.getStatus() + ".");
        }
        return submission;
    }

    private void notifyCustomer(PaymentSubmission submission, String message) {
        Customer customer = submission.getCustomer();
        Notification notification = Notification.builder()
                .customer(customer)
                .type(NotificationType.PAYMENT_CONFIRM)
                .channel(NotificationChannel.PUSH)
                .message(message)
                .sent(true)
                .sentAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
    }

    // ── Mapping ──────────────────────────────────────────────────────────────────

    private PaymentSubmissionResponse toResponse(PaymentSubmission s) {
        return PaymentSubmissionResponse.builder()
                .id(s.getId())
                .ticketId(s.getTicket().getId())
                .ticketNumber(s.getTicket().getTicketNumber())
                .shopName(s.getTicket().getShop().getName())
                .customerId(s.getCustomer().getId())
                .customerName(s.getCustomer().getFullName())
                .amount(s.getAmount())
                .paymentType(s.getPaymentType())
                .bankName(s.getBankName())
                .referenceNumber(s.getReferenceNumber())
                .receiptUrl(s.getReceiptUrl())
                .status(s.getStatus())
                .rejectionReason(s.getRejectionReason())
                .resultingPaymentId(s.getResultingPaymentId())
                .submittedAt(s.getSubmittedAt())
                .reviewedAt(s.getReviewedAt())
                .build();
    }
}