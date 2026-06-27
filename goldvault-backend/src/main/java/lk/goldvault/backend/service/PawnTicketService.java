package lk.goldvault.backend.service;

import lk.goldvault.backend.dto.request.GoldItemRequest;
import lk.goldvault.backend.dto.request.PawnTicketRequest;
import lk.goldvault.backend.dto.request.RenewalRequest;
import lk.goldvault.backend.dto.response.GoldItemResponse;
import lk.goldvault.backend.dto.response.PawnTicketResponse;
import lk.goldvault.backend.entity.*;
import lk.goldvault.backend.enums.InterestType;
import lk.goldvault.backend.enums.PaymentMethod;
import lk.goldvault.backend.enums.PaymentType;
import lk.goldvault.backend.enums.TicketStatus;
import lk.goldvault.backend.repository.BranchRepository;
import lk.goldvault.backend.repository.CustomerRepository;
import lk.goldvault.backend.repository.PawnTicketRepository;
import lk.goldvault.backend.repository.PaymentRepository;
import lk.goldvault.backend.util.QrCodeUtil;
import lk.goldvault.backend.util.TicketNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PawnTicketService {

    private final PawnTicketRepository      pawnTicketRepository;
    private final CustomerRepository        customerRepository;
    private final BranchRepository          branchRepository;
    private final PaymentRepository         paymentRepository;
    private final TicketNumberGenerator     ticketNumberGenerator;
    private final QrCodeUtil                qrCodeUtil;
    private final InterestCalculatorService interestCalculatorService;

    // ── Grant ────────────────────────────────────────────────────────────────────

    public PawnTicketResponse grantTicket(Long shopId, PawnTicketRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException(
                        "Customer not found with id: " + request.getCustomerId()));

        Branch branch = null;
        if (request.getBranchId() != null) {
            branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new RuntimeException(
                            "Branch not found with id: " + request.getBranchId()));
        }

        LocalDate pawnDate   = LocalDate.now();
        LocalDate expiryDate = pawnDate.plusMonths(request.getPeriodMonths());
        String ticketNumber  = ticketNumberGenerator.generate();

        PawnTicket newTicket = PawnTicket.builder()
                .ticketNumber(ticketNumber)
                .customer(customer)
                .shop(customer.getShop())
                .branch(branch)
                .loanAmount(request.getLoanAmount())
                .interestRate(request.getInterestRate())
                .interestType(request.getInterestType() != null
                        ? request.getInterestType() : InterestType.FLAT)
                .pawnDate(pawnDate)
                .expiryDate(expiryDate)
                .status(TicketStatus.ACTIVE)
                .notes(request.getNotes())
                .build();

        String qrContent = "GOLDVAULT-TICKET:" + ticketNumber;
        newTicket.setQrCode(qrCodeUtil.generateQrBase64(qrContent));
        newTicket.setGoldItems(buildGoldItems(request.getGoldItems(), newTicket));

        PawnTicket saved = pawnTicketRepository.save(newTicket);
        return toResponse(saved);
    }

    // ── Renewal / Extension ───────────────────────────────────────────────────────

    @Transactional
    public PawnTicketResponse renewTicket(Long ticketId, RenewalRequest request) {
        PawnTicket ticket = pawnTicketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + ticketId));

        // Only ACTIVE or EXPIRED tickets can be renewed
        if (ticket.getStatus() != TicketStatus.ACTIVE
                && ticket.getStatus() != TicketStatus.EXPIRED) {
            throw new RuntimeException(
                    "Only ACTIVE or EXPIRED tickets can be extended. Current status: "
                            + ticket.getStatus());
        }

        LocalDate today = LocalDate.now();

        // Validate the interest payment covers at least the accrued interest
        BigDecimal accruedInterest = interestCalculatorService
                .calculateAccruedInterest(ticket, today);

        if (request.getInterestPaid().compareTo(accruedInterest) < 0) {
            throw new RuntimeException(
                    String.format(
                        "Interest paid (LKR %.2f) is less than accrued interest (LKR %.2f). " +
                        "Customer must pay at least the accrued interest to renew.",
                        request.getInterestPaid(), accruedInterest));
        }

        // Record the renewal interest payment
        Payment renewalPayment = Payment.builder()
                .ticket(ticket)
                .amount(request.getInterestPaid())
                .paymentType(PaymentType.RENEWAL)
                .paymentMethod(request.getPaymentMethod() != null
                        ? request.getPaymentMethod() : PaymentMethod.CASH)
                .referenceNumber(request.getReferenceNumber())
                .paymentDate(LocalDateTime.now())
                .build();

        paymentRepository.save(renewalPayment);
        ticket.getPayments().add(renewalPayment);

        // Extend the expiry date
        // If already expired, extend from today; if still active, extend from current expiry
        LocalDate baseDate = ticket.getExpiryDate().isBefore(today)
                ? today : ticket.getExpiryDate();
        LocalDate newExpiry = baseDate.plusMonths(request.getExtensionMonths());

        ticket.setExpiryDate(newExpiry);

        // Reactivate if it was expired
        if (ticket.getStatus() == TicketStatus.EXPIRED) {
            ticket.setStatus(TicketStatus.ACTIVE);
        }

        // Append renewal note
        String renewalNote = String.format(
            "[RENEWED %s] Extended by %d month(s). New expiry: %s. Interest paid: LKR %.2f.",
            today, request.getExtensionMonths(), newExpiry,
            request.getInterestPaid()
        );
        String existingNotes = ticket.getNotes() != null ? ticket.getNotes() : "";
        ticket.setNotes(existingNotes.isEmpty() ? renewalNote : existingNotes + "\n" + renewalNote);

        PawnTicket saved = pawnTicketRepository.save(ticket);
        return toResponse(saved);
    }

    // ── Read ─────────────────────────────────────────────────────────────────────

    public PawnTicketResponse getById(Long id) {
        PawnTicket ticket = pawnTicketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + id));
        return toResponse(ticket);
    }

    public PawnTicketResponse getByTicketNumber(String ticketNumber) {
        PawnTicket ticket = pawnTicketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + ticketNumber));
        return toResponse(ticket);
    }

    public List<PawnTicketResponse> getByShop(Long shopId) {
        return pawnTicketRepository.findByShopId(shopId)
                .stream().map(this::toResponse).toList();
    }

    public List<PawnTicketResponse> getByShopAndStatus(Long shopId, TicketStatus status) {
        return pawnTicketRepository.findByShopIdAndStatus(shopId, status)
                .stream().map(this::toResponse).toList();
    }

    public List<PawnTicketResponse> getByCustomer(Long customerId) {
        return pawnTicketRepository.findByCustomerId(customerId)
                .stream().map(this::toResponse).toList();
    }

    // ── Redeem ───────────────────────────────────────────────────────────────────

    public PawnTicketResponse redeem(Long id) {
        PawnTicket ticket = pawnTicketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + id));

        if (ticket.getStatus() != TicketStatus.ACTIVE) {
            throw new RuntimeException(
                    "Only ACTIVE tickets can be redeemed. Current status: " + ticket.getStatus());
        }

        ticket.setStatus(TicketStatus.REDEEMED);
        return toResponse(pawnTicketRepository.save(ticket));
    }

    /** Marks all active, past-expiry tickets as EXPIRED. Intended for daily schedule. */
    public int markExpiredTickets() {
        List<PawnTicket> overdue = pawnTicketRepository
                .findByStatusAndExpiryDateBefore(TicketStatus.ACTIVE, LocalDate.now());
        overdue.forEach(t -> t.setStatus(TicketStatus.EXPIRED));
        pawnTicketRepository.saveAll(overdue);
        return overdue.size();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private List<GoldItem> buildGoldItems(List<GoldItemRequest> requests, PawnTicket ticket) {
        return requests.stream()
                .map(req -> toGoldItemEntity(req, ticket))
                .toList();
    }

    private GoldItem toGoldItemEntity(GoldItemRequest req, PawnTicket ticket) {
        return GoldItem.builder()
                .ticket(ticket)
                .description(req.getDescription())
                .goldType(req.getGoldType())
                .weightGrams(req.getWeightGrams())
                .purity(req.getPurity())
                .estimatedValue(req.getEstimatedValue())
                .photoUrl(req.getPhotoUrl())
                .build();
    }

    private PawnTicketResponse toResponse(PawnTicket ticket) {
        LocalDate today = LocalDate.now();

        List<GoldItemResponse> itemResponses = ticket.getGoldItems() == null ? List.of() :
                ticket.getGoldItems().stream()
                        .map(item -> GoldItemResponse.builder()
                                .id(item.getId())
                                .description(item.getDescription())
                                .goldType(item.getGoldType())
                                .weightGrams(item.getWeightGrams())
                                .purity(item.getPurity())
                                .estimatedValue(item.getEstimatedValue())
                                .photoUrl(item.getPhotoUrl())
                                .build())
                        .toList();

        // Count renewals from payment history
        int renewalCount = ticket.getPayments() == null ? 0 :
                (int) ticket.getPayments().stream()
                        .filter(p -> p.getPaymentType() == PaymentType.RENEWAL)
                        .count();

        BigDecimal accruedInterest = interestCalculatorService
                .calculateAccruedInterest(ticket, today);

        return PawnTicketResponse.builder()
                .id(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .customerId(ticket.getCustomer().getId())
                .customerName(ticket.getCustomer().getFullName())
                .customerNic(ticket.getCustomer().getNic())
                .shopId(ticket.getShop().getId())
                .branchId(ticket.getBranch() != null ? ticket.getBranch().getId() : null)
                .loanAmount(ticket.getLoanAmount())
                .interestRate(ticket.getInterestRate())
                .interestType(ticket.getInterestType())
                .pawnDate(ticket.getPawnDate())
                .expiryDate(ticket.getExpiryDate())
                .status(ticket.getStatus())
                .qrCode(ticket.getQrCode())
                .notes(ticket.getNotes())
                .goldItems(itemResponses)
                .totalPaid(interestCalculatorService.totalPaid(ticket))
                .outstandingBalance(interestCalculatorService.calculateOutstandingBalance(ticket, today))
                .daysElapsed(interestCalculatorService.daysElapsed(ticket, today))
                .overdue(interestCalculatorService.isOverdue(ticket, today))
                .renewalCount(renewalCount)
                .accruedInterestToday(accruedInterest)
                .build();
    }
}