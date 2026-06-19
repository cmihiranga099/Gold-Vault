package lk.goldvault.backend.service;

import lk.goldvault.backend.dto.request.GoldItemRequest;
import lk.goldvault.backend.dto.request.PawnTicketRequest;
import lk.goldvault.backend.dto.response.GoldItemResponse;
import lk.goldvault.backend.dto.response.PawnTicketResponse;
import lk.goldvault.backend.entity.*;
import lk.goldvault.backend.enums.InterestType;
import lk.goldvault.backend.enums.TicketStatus;
import lk.goldvault.backend.repository.BranchRepository;
import lk.goldvault.backend.repository.CustomerRepository;
import lk.goldvault.backend.repository.PawnTicketRepository;
import lk.goldvault.backend.util.QrCodeUtil;
import lk.goldvault.backend.util.TicketNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PawnTicketService {

    private final PawnTicketRepository pawnTicketRepository;
    private final CustomerRepository customerRepository;
    private final BranchRepository branchRepository;
    private final TicketNumberGenerator ticketNumberGenerator;
    private final QrCodeUtil qrCodeUtil;
    private final InterestCalculatorService interestCalculatorService;

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

        LocalDate pawnDate = LocalDate.now();
        LocalDate expiryDate = pawnDate.plusMonths(request.getPeriodMonths());
        String ticketNumber = ticketNumberGenerator.generate();

        PawnTicket ticket = PawnTicket.builder()
                .ticketNumber(ticketNumber)
                .customer(customer)
                .shop(customer.getShop())
                .branch(branch)
                .loanAmount(request.getLoanAmount())
                .interestRate(request.getInterestRate())
                .interestType(request.getInterestType() != null ? request.getInterestType() : InterestType.FLAT)
                .pawnDate(pawnDate)
                .expiryDate(expiryDate)
                .status(TicketStatus.ACTIVE)
                .notes(request.getNotes())
                .build();

        // Generate QR code pointing to the ticket lookup URL, using the ticket number
        String qrContent = "GOLDVAULT-TICKET:" + ticketNumber;
        ticket.setQrCode(qrCodeUtil.generateQrBase64(qrContent));

        // Attach gold items
        List<GoldItem> items = request.getGoldItems().stream()
                .map(itemReq -> toGoldItemEntity(itemReq, ticket))
                .toList();
        ticket.setGoldItems(items);

        ticket = pawnTicketRepository.save(ticket);
        return toResponse(ticket);
    }

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
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<PawnTicketResponse> getByShopAndStatus(Long shopId, TicketStatus status) {
        return pawnTicketRepository.findByShopIdAndStatus(shopId, status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<PawnTicketResponse> getByCustomer(Long customerId) {
        return pawnTicketRepository.findByCustomerId(customerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public PawnTicketResponse redeem(Long id) {
        PawnTicket ticket = pawnTicketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + id));

        if (ticket.getStatus() != TicketStatus.ACTIVE) {
            throw new RuntimeException("Only ACTIVE tickets can be redeemed. Current status: " + ticket.getStatus());
        }

        ticket.setStatus(TicketStatus.REDEEMED);
        ticket = pawnTicketRepository.save(ticket);
        return toResponse(ticket);
    }

    /** Marks all active, past-expiry tickets as EXPIRED. Intended to run on a daily schedule. */
    public int markExpiredTickets() {
        List<PawnTicket> overdue = pawnTicketRepository
                .findByStatusAndExpiryDateBefore(TicketStatus.ACTIVE, LocalDate.now());
        overdue.forEach(t -> t.setStatus(TicketStatus.EXPIRED));
        pawnTicketRepository.saveAll(overdue);
        return overdue.size();
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
                .build();
    }
}