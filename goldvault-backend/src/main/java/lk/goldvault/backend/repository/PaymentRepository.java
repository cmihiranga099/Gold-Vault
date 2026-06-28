package lk.goldvault.backend.repository;

import lk.goldvault.backend.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByTicketId(Long ticketId);
    List<Payment> findByPaymentDateBetween(LocalDateTime start, LocalDateTime end);

    // ── Analytics ───────────────────────────────────────────────────────────────

    /** Total collected for a shop in a date range */
    @Query("""
        SELECT COALESCE(SUM(p.amount), 0)
        FROM Payment p
        WHERE p.ticket.shop.id = :shopId
          AND p.paymentDate    BETWEEN :from AND :to
        """)
    BigDecimal totalCollectedByShop(
            @Param("shopId") Long shopId,
            @Param("from")   LocalDateTime from,
            @Param("to")     LocalDateTime to);

    /** Monthly collection totals for last N months */
    @Query(value = """
        SELECT DATE_FORMAT(p.payment_date, '%Y-%m') AS month,
               SUM(p.amount) AS total
        FROM payment p
        JOIN pawn_ticket t ON p.ticket_id = t.id
        WHERE t.shop_id = :shopId
          AND p.payment_date >= :from
        GROUP BY DATE_FORMAT(p.payment_date, '%Y-%m')
        ORDER BY month ASC
        """, nativeQuery = true)
    List<Object[]> monthlyCollectionByShop(
            @Param("shopId") Long shopId,
            @Param("from")   LocalDateTime from);

    /** Collection broken down by payment type for a shop in a range */
    @Query("""
        SELECT p.paymentType, COALESCE(SUM(p.amount), 0)
        FROM Payment p
        WHERE p.ticket.shop.id = :shopId
          AND p.paymentDate    BETWEEN :from AND :to
        GROUP BY p.paymentType
        """)
    List<Object[]> collectionByTypeForShop(
            @Param("shopId") Long shopId,
            @Param("from")   LocalDateTime from,
            @Param("to")     LocalDateTime to);
}