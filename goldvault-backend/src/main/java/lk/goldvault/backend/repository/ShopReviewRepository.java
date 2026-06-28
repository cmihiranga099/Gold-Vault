package lk.goldvault.backend.repository;

import lk.goldvault.backend.entity.ShopReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopReviewRepository extends JpaRepository<ShopReview, Long> {

    /** All visible reviews for a shop, newest first */
    List<ShopReview> findByShopIdAndStatusOrderByCreatedAtDesc(Long shopId, String status);

    /** All reviews for a shop (admin view — includes hidden) */
    List<ShopReview> findByShopIdOrderByCreatedAtDesc(Long shopId);

    /** Check if customer already reviewed this ticket */
    boolean existsByTicketId(Long ticketId);

    /** Get review for a specific ticket */
    Optional<ShopReview> findByTicketId(Long ticketId);

    /** Average rating for a shop (visible only) */
    @Query("SELECT AVG(r.rating) FROM ShopReview r WHERE r.shop.id = :shopId AND r.status = 'VISIBLE'")
    Double averageRatingByShop(@Param("shopId") Long shopId);

    /** Review count for a shop (visible only) */
    long countByShopIdAndStatus(Long shopId, String status);

    /** Rating distribution — for star breakdown chart */
    @Query("SELECT r.rating, COUNT(r) FROM ShopReview r WHERE r.shop.id = :shopId AND r.status = 'VISIBLE' GROUP BY r.rating ORDER BY r.rating DESC")
    List<Object[]> ratingDistributionByShop(@Param("shopId") Long shopId);
}