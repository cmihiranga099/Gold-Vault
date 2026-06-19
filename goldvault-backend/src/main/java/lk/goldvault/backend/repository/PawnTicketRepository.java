package lk.goldvault.backend.repository;

import lk.goldvault.backend.entity.PawnTicket;
import lk.goldvault.backend.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PawnTicketRepository extends JpaRepository<PawnTicket, Long> {
    Optional<PawnTicket> findByTicketNumber(String ticketNumber);
    List<PawnTicket> findByShopId(Long shopId);
    List<PawnTicket> findByCustomerId(Long customerId);
    List<PawnTicket> findByShopIdAndStatus(Long shopId, TicketStatus status);
    List<PawnTicket> findByStatusAndExpiryDateBetween(
            TicketStatus status, LocalDate start, LocalDate end);
    List<PawnTicket> findByStatusAndExpiryDateBefore(TicketStatus status, LocalDate date);
}