package lk.goldvault.backend.repository;

import lk.goldvault.backend.entity.PawnTicket;
import lk.goldvault.backend.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    List<PawnTicket> findByStatusAndExpiryDateBetween(TicketStatus status, LocalDate start, LocalDate end);
    List<PawnTicket> findByStatusAndExpiryDateBefore(TicketStatus status, LocalDate date);

    // ── Analytics ───────────────────────────────────────────────────────────────

    long countByShopIdAndStatus(Long shopId, TicketStatus status);

    /** Total loan amount for all ACTIVE tickets in a shop */
    @Query("SELECT COALESCE(SUM(t.loanAmount), 0) FROM PawnTicket t WHERE t.shop.id = :shopId AND t.status = 'ACTIVE'")
    java.math.BigDecimal sumActiveLoanAmountByShop(@Param("shopId") Long shopId);

    /** Count tickets granted per month for the last N months */
    @Query(value = """
        SELECT DATE_FORMAT(pawn_date, '%Y-%m') AS month,
               COUNT(*) AS count
        FROM pawn_ticket
        WHERE shop_id = :shopId
          AND pawn_date >= :from
        GROUP BY DATE_FORMAT(pawn_date, '%Y-%m')
        ORDER BY month ASC
        """, nativeQuery = true)
    List<Object[]> ticketsGrantedPerMonth(@Param("shopId") Long shopId, @Param("from") LocalDate from);

    /** Redemption rate: redeemed vs total closed (redeemed + expired + auctioned) */
    @Query("""
        SELECT t.status, COUNT(t)
        FROM PawnTicket t
        WHERE t.shop.id = :shopId
          AND t.status IN ('ACTIVE','REDEEMED','EXPIRED','AUCTIONED')
        GROUP BY t.status
        """)
    List<Object[]> statusBreakdownByShop(@Param("shopId") Long shopId);

    /** Top 5 customers by total loan amount for this shop */
    @Query("""
        SELECT t.customer.fullName, COUNT(t), SUM(t.loanAmount)
        FROM PawnTicket t
        WHERE t.shop.id = :shopId
        GROUP BY t.customer.id, t.customer.fullName
        ORDER BY SUM(t.loanAmount) DESC
        """)
    List<Object[]> topCustomersByLoanVolume(@Param("shopId") Long shopId);

    /** Tickets expiring in next :days days */
    @Query("""
        SELECT t FROM PawnTicket t
        WHERE t.shop.id  = :shopId
          AND t.status   = 'ACTIVE'
          AND t.expiryDate BETWEEN :today AND :until
        ORDER BY t.expiryDate ASC
        """)
    List<PawnTicket> findExpiringSoon(
            @Param("shopId") Long shopId,
            @Param("today")  LocalDate today,
            @Param("until")  LocalDate until);
}