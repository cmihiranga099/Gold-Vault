package lk.goldvault.backend.service;

import lk.goldvault.backend.dto.response.ShopDashboardResponse;
import lk.goldvault.backend.entity.PawnTicket;
import lk.goldvault.backend.enums.TicketStatus;
import lk.goldvault.backend.repository.CustomerRepository;
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
public class ShopDashboardService {

    private final CustomerRepository customerRepository;
    private final PawnTicketRepository pawnTicketRepository;
    private final PaymentRepository paymentRepository;
    private final InterestCalculatorService interestCalculatorService;

    public ShopDashboardResponse getSummary(Long shopId) {
        long totalCustomers = customerRepository.findByShopId(shopId).size();

        List<PawnTicket> shopTickets = pawnTicketRepository.findByShopId(shopId);

        long activeTickets = shopTickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.ACTIVE)
                .count();

        long expiredTickets = shopTickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.EXPIRED)
                .count();

        LocalDate today = LocalDate.now();
        LocalDate in7Days = today.plusDays(7);
        long expiringSoonCount = shopTickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.ACTIVE)
                .filter(t -> !t.getExpiryDate().isBefore(today) && !t.getExpiryDate().isAfter(in7Days))
                .count();

        BigDecimal totalOutstanding = shopTickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.ACTIVE || t.getStatus() == TicketStatus.EXPIRED)
                .map(t -> interestCalculatorService.calculateOutstandingBalance(t, today))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDateTime startOfToday = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime endOfToday = LocalDateTime.of(today, LocalTime.MAX);

        List<lk.goldvault.backend.entity.Payment> todayPayments = paymentRepository
                .findByPaymentDateBetween(startOfToday, endOfToday)
                .stream()
                .filter(p -> p.getTicket().getShop().getId().equals(shopId))
                .toList();

        BigDecimal todayCollection = todayPayments.stream()
                .map(lk.goldvault.backend.entity.Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return ShopDashboardResponse.builder()
                .totalCustomers(totalCustomers)
                .activeTickets(activeTickets)
                .expiringSoonCount(expiringSoonCount)
                .expiredTickets(expiredTickets)
                .totalOutstandingLoans(totalOutstanding)
                .todayCollection(todayCollection)
                .todayPaymentCount(todayPayments.size())
                .build();
    }
}