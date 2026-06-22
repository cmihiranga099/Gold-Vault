package lk.goldvault.backend.service;

import lk.goldvault.backend.dto.response.DashboardSummaryResponse;
import lk.goldvault.backend.entity.PawnTicket;
import lk.goldvault.backend.enums.ShopStatus;
import lk.goldvault.backend.enums.TicketStatus;
import lk.goldvault.backend.repository.CustomerRepository;
import lk.goldvault.backend.repository.PawnShopRepository;
import lk.goldvault.backend.repository.PawnTicketRepository;
import lk.goldvault.backend.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final PawnShopRepository pawnShopRepository;
    private final CustomerRepository customerRepository;
    private final PawnTicketRepository pawnTicketRepository;
    private final PaymentRepository paymentRepository;
    private final InterestCalculatorService interestCalculatorService;

    public DashboardSummaryResponse getSummary() {
        long totalShops = pawnShopRepository.count();
        long pendingShops = pawnShopRepository.findByStatus(ShopStatus.PENDING).size();
        long activeShops = pawnShopRepository.findByStatus(ShopStatus.ACTIVE).size();
        long totalCustomers = customerRepository.count();

        List<PawnTicket> allTickets = pawnTicketRepository.findAll();
        long activeTickets = allTickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.ACTIVE)
                .count();
        long expiredTickets = allTickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.EXPIRED)
                .count();
        long redeemedTickets = allTickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.REDEEMED)
                .count();

        BigDecimal totalOutstanding = allTickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.ACTIVE || t.getStatus() == TicketStatus.EXPIRED)
                .map(t -> interestCalculatorService.calculateOutstandingBalance(t, LocalDate.now()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        BigDecimal todayCollection = paymentRepository
                .findByPaymentDateBetween(startOfToday, endOfToday)
                .stream()
                .map(p -> p.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DashboardSummaryResponse.builder()
                .totalShops(totalShops)
                .pendingShopApprovals(pendingShops)
                .activeShops(activeShops)
                .totalCustomers(totalCustomers)
                .activeTickets(activeTickets)
                .expiredTickets(expiredTickets)
                .redeemedTickets(redeemedTickets)
                .totalOutstandingLoans(totalOutstanding)
                .todayCollection(todayCollection)
                .build();
    }
}