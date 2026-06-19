package lk.goldvault.backend.repository;

import lk.goldvault.backend.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByTicketId(Long ticketId);
    List<Payment> findByPaymentDateBetween(LocalDateTime start, LocalDateTime end);
}