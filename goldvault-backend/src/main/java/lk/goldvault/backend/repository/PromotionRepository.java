package lk.goldvault.backend.repository;

import lk.goldvault.backend.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    List<Promotion> findByShopIdOrderByCreatedAtDesc(Long shopId);

    /** Currently live promotions for a shop */
    @Query("""
        SELECT p FROM Promotion p
        WHERE p.shop.id = :shopId
          AND p.status  = 'ACTIVE'
          AND p.startsAt <= :now
          AND p.endsAt   >= :now
        ORDER BY p.endsAt ASC
        """)
    List<Promotion> findActiveByShop(
            @Param("shopId") Long shopId,
            @Param("now")    LocalDateTime now);

    /** All live promotions across the platform — for admin + customer feed */
    @Query("""
        SELECT p FROM Promotion p
        WHERE p.status  = 'ACTIVE'
          AND p.startsAt <= :now
          AND p.endsAt   >= :now
        ORDER BY p.endsAt ASC
        """)
    List<Promotion> findAllActive(@Param("now") LocalDateTime now);

    /** Auto-expire: find ACTIVE promotions whose end time has passed */
    @Query("""
        SELECT p FROM Promotion p
        WHERE p.status = 'ACTIVE'
          AND p.endsAt < :now
        """)
    List<Promotion> findExpired(@Param("now") LocalDateTime now);
}