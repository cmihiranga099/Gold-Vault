package lk.goldvault.backend.repository;

import lk.goldvault.backend.entity.AmlFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AmlFlagRepository extends JpaRepository<AmlFlag, Long> {

    List<AmlFlag> findByStatusOrderByCreatedAtDesc(String status);
    List<AmlFlag> findAllByOrderByCreatedAtDesc();
    List<AmlFlag> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    /** Avoid duplicate flags of same type for same customer+ticket on same day */
    boolean existsByCustomerIdAndFlagTypeAndTicketIdAndCreatedAtAfter(
            Long customerId, String flagType, Long ticketId, LocalDateTime after);

    /** Count OPEN flags for dashboard KPI */
    long countByStatus(String status);

    /** Flag breakdown by type */
    @Query("SELECT f.flagType, COUNT(f) FROM AmlFlag f GROUP BY f.flagType ORDER BY COUNT(f) DESC")
    List<Object[]> countByFlagType();
}