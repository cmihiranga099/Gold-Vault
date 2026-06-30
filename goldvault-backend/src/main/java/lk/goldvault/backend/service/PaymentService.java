package lk.goldvault.backend.service;

import lk.goldvault.backend.dto.request.PaymentRequest;
import lk.goldvault.backend.dto.response.DailyCollectionResponse;
import lk.goldvault.backend.dto.response.PaymentResponse;
import lk.goldvault.backend.entity.PawnTicket;
import lk.goldvault.backend.entity.Payment;
import lk.goldvault.backend.enums.PaymentMethod;
import lk.goldvault.backend.enums.PaymentType;
import lk.goldvault.backend.enums.TicketStatus;
import lk.goldvault.backend.repository.PawnTicketRepository;
import lk.goldvault.backend.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PawnTicketRepository pawnTicketRepository;
    private final InterestCalculatorService interestCalculatorService;
    private final LoyaltyService loyaltyService;

    @Transactional
    public PaymentResponse recordPayment(PaymentRequest request) {
        PawnTicket ticket = pawnTicketRepository.findById(request.getTicketId())
                .orElseThrow(() -> new RuntimeException(
                        "Ticket not found with id: " + request.getTicketId()));

        if (ticket.getStatus() != TicketStatus.ACTIVE && ticket.getStatus() != TicketStatus.EXPIRED) {
            throw new RuntimeException(
                    "Cannot record payment on a ticket with status: " + ticket.getStatus());
        }

        BigDecimal balanceBeforePayment = interestCalculatorService
                .calculateOutstandingBalance(ticket, LocalDate.now());

        if (request.getAmount().compareTo(balanceBeforePayment) > 0
                && request.getPaymentType() != PaymentType.FULL_REDEMPTION) {
            throw new RuntimeException(
                    "Payment amount (" + request.getAmount()
                            + ") exceeds outstanding balance (" + balanceBeforePayment + ")");
        }

        Payment payment = Payment.builder()
                .ticket(ticket)
                .amount(request.getAmount())
                .paymentType(request.getPaymentType())
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : PaymentMethod.CASH)
                .referenceNumber(request.getReferenceNumber())
                .paymentDate(LocalDateTime.now())
                .receivedBy(request.getReceivedBy())
                .build();

        loyaltyService.awardForPayment(ticket, request.getAmount());

        // Attach to ticket's in-memory payment list so the balance recalculation below sees it
        ticket.getPayments().add(payment);

        BigDecimal balanceAfterPayment = interestCalculatorService
                .calculateOutstandingBalance(ticket, LocalDate.now());

        boolean redeemed = false;
        if (request.getPaymentType() == PaymentType.FULL_REDEMPTION
                || balanceAfterPayment.compareTo(BigDecimal.ZERO) <= 0) {
            ticket.setStatus(TicketStatus.REDEEMED);
            pawnTicketRepository.save(ticket);
            redeemed = true;
            balanceAfterPayment = BigDecimal.ZERO;
        }

        return toResponse(payment, ticket, balanceAfterPayment, redeemed);
    }

    public List<PaymentResponse> getByTicket(Long ticketId) {
        PawnTicket ticket = pawnTicketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        return paymentRepository.findByTicketId(ticketId)
                .stream()
                .map(p -> toResponse(p, ticket, null, ticket.getStatus() == TicketStatus.REDEEMED))
                .toList();
    }

    /** Daily collection report for a given date — used on the shop dashboard. */
    public DailyCollectionResponse getDailyCollection(LocalDate date) {
        LocalDateTime startOfDay = LocalDateTime.of(date, LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(date, LocalTime.MAX);

        List<Payment> payments = paymentRepository.findByPaymentDateBetween(startOfDay, endOfDay);

        BigDecimal totalCollected = sumByType(payments, null);
        BigDecimal totalInterest = sumByType(payments, PaymentType.INTEREST);
        BigDecimal totalPartial = sumByType(payments, PaymentType.PARTIAL);
        BigDecimal totalFullRedemption = sumByType(payments, PaymentType.FULL_REDEMPTION);

        List<PaymentResponse> paymentResponses = payments.stream()
                .map(p -> toResponse(p, p.getTicket(), null, p.getTicket().getStatus() == TicketStatus.REDEEMED))
                .toList();

        return DailyCollectionResponse.builder()
                .date(date)
                .totalCollected(totalCollected)
                .paymentCount(payments.size())
                .totalInterest(totalInterest)
                .totalPartial(totalPartial)
                .totalFullRedemption(totalFullRedemption)
                .payments(paymentResponses)
                .build();
    }

    private BigDecimal sumByType(List<Payment> payments, PaymentType type) {
        return payments.stream()
                .filter(p -> type == null || p.getPaymentType() == type)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private PaymentResponse toResponse(Payment payment, PawnTicket ticket,
                                         BigDecimal remainingBalance, boolean redeemed) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .ticketId(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .amount(payment.getAmount())
                .paymentType(payment.getPaymentType())
                .paymentMethod(payment.getPaymentMethod())
                .referenceNumber(payment.getReferenceNumber())
                .paymentDate(payment.getPaymentDate())
                .receivedBy(payment.getReceivedBy())
                .remainingBalance(remainingBalance != null ? remainingBalance :
                        interestCalculatorService.calculateOutstandingBalance(ticket, LocalDate.now()))
                .ticketRedeemed(redeemed)
                .build();
    }
}