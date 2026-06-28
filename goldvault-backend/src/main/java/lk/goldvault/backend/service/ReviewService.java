package lk.goldvault.backend.service;

import lk.goldvault.backend.dto.request.ReviewRequest;
import lk.goldvault.backend.dto.response.ReviewResponse;
import lk.goldvault.backend.dto.response.ShopRatingResponse;
import lk.goldvault.backend.entity.Customer;
import lk.goldvault.backend.entity.PawnTicket;
import lk.goldvault.backend.entity.ShopReview;
import lk.goldvault.backend.enums.TicketStatus;
import lk.goldvault.backend.repository.CustomerRepository;
import lk.goldvault.backend.repository.PawnTicketRepository;
import lk.goldvault.backend.repository.ShopReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ShopReviewRepository shopReviewRepository;
    private final PawnTicketRepository  pawnTicketRepository;
    private final CustomerRepository    customerRepository;

    // ── Submit a review ──────────────────────────────────────────────────────────

    @Transactional
    public ReviewResponse submitReview(Long customerId, ReviewRequest request) {
        // Load ticket
        PawnTicket ticket = pawnTicketRepository.findById(request.getTicketId())
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + request.getTicketId()));

        // Only redeemed tickets can be reviewed
        if (ticket.getStatus() != TicketStatus.REDEEMED) {
            throw new RuntimeException(
                "You can only review a shop after your ticket is fully redeemed.");
        }

        // Ticket must belong to this customer
        if (!ticket.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("This ticket does not belong to your account.");
        }

        // One review per ticket
        if (shopReviewRepository.existsByTicketId(ticket.getId())) {
            throw new RuntimeException("You have already reviewed this ticket.");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));

        ShopReview review = ShopReview.builder()
                .shop(ticket.getShop())
                .customer(customer)
                .ticket(ticket)
                .rating(request.getRating())
                .comment(request.getComment())
                .status("VISIBLE")
                .build();

        ShopReview saved = shopReviewRepository.save(review);
        return toResponse(saved);
    }

    // ── Public: visible reviews for a shop ───────────────────────────────────────

    public List<ReviewResponse> getVisibleReviews(Long shopId) {
        return shopReviewRepository
                .findByShopIdAndStatusOrderByCreatedAtDesc(shopId, "VISIBLE")
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ── Shop rating summary ───────────────────────────────────────────────────────

    public ShopRatingResponse getShopRating(Long shopId) {
        Double avg    = shopReviewRepository.averageRatingByShop(shopId);
        long   count  = shopReviewRepository.countByShopIdAndStatus(shopId, "VISIBLE");

        // Build rating distribution map 5→1
        Map<Integer, Long> dist = new LinkedHashMap<>();
        for (int i = 5; i >= 1; i--) dist.put(i, 0L);
        shopReviewRepository.ratingDistributionByShop(shopId)
                .forEach(row -> dist.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue()));

        // Load shop name
        String shopName = shopReviewRepository.findByShopIdAndStatusOrderByCreatedAtDesc(shopId, "VISIBLE")
                .stream().findFirst()
                .map(r -> r.getShop().getName())
                .orElse("Shop #" + shopId);

        return ShopRatingResponse.builder()
                .shopId(shopId)
                .shopName(shopName)
                .averageRating(avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0)
                .totalReviews(count)
                .ratingDistribution(dist)
                .build();
    }

    // ── Check if customer already reviewed a ticket ───────────────────────────────

    public boolean hasReviewed(Long ticketId) {
        return shopReviewRepository.existsByTicketId(ticketId);
    }

    // ── Admin: get all reviews for a shop (including hidden) ─────────────────────

    public List<ReviewResponse> getAllReviewsForAdmin(Long shopId) {
        return shopReviewRepository.findByShopIdOrderByCreatedAtDesc(shopId)
                .stream().map(this::toResponse).toList();
    }

    // ── Admin: hide or show a review ─────────────────────────────────────────────

    @Transactional
    public ReviewResponse setReviewStatus(Long reviewId, String status) {
        ShopReview review = shopReviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found: " + reviewId));
        review.setStatus(status.toUpperCase());
        return toResponse(shopReviewRepository.save(review));
    }

    // ── Helper ───────────────────────────────────────────────────────────────────

    private ReviewResponse toResponse(ShopReview r) {
        return ReviewResponse.builder()
                .id(r.getId())
                .shopId(r.getShop().getId())
                .shopName(r.getShop().getName())
                .customerId(r.getCustomer().getId())
                .customerName(r.getCustomer().getFullName())
                .ticketId(r.getTicket().getId())
                .ticketNumber(r.getTicket().getTicketNumber())
                .rating(r.getRating())
                .comment(r.getComment())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .build();
    }
}