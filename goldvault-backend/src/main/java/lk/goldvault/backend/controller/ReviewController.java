package lk.goldvault.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.goldvault.backend.dto.request.ReviewRequest;
import lk.goldvault.backend.dto.response.ApiResponse;
import lk.goldvault.backend.dto.response.ReviewResponse;
import lk.goldvault.backend.dto.response.ShopRatingResponse;
import lk.goldvault.backend.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Customer reviews and shop ratings")
public class ReviewController {

    private final ReviewService reviewService;

    // ── Customer submits a review ─────────────────────────────────────────────
    @PostMapping("/api/customer/reviews")
    @Operation(summary = "Submit a review for a redeemed ticket")
    public ResponseEntity<ApiResponse<ReviewResponse>> submitReview(
            @RequestParam Long customerId,
            @Valid @RequestBody ReviewRequest request) {
        ReviewResponse response = reviewService.submitReview(customerId, request);
        return ResponseEntity.ok(ApiResponse.success("Review submitted successfully.", response));
    }

    // ── Check if ticket already reviewed ─────────────────────────────────────
    @GetMapping("/api/customer/reviews/check/{ticketId}")
    @Operation(summary = "Check if customer already reviewed this ticket")
    public ResponseEntity<ApiResponse<Boolean>> hasReviewed(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.hasReviewed(ticketId)));
    }

    // ── Public: visible reviews for a shop ────────────────────────────────────
    @GetMapping("/api/public/shops/{shopId}/reviews")
    @Operation(summary = "Get visible reviews for a shop")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getVisibleReviews(
            @PathVariable Long shopId) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.getVisibleReviews(shopId)));
    }

    // ── Public: shop rating summary ───────────────────────────────────────────
    @GetMapping("/api/public/shops/{shopId}/rating")
    @Operation(summary = "Get average rating and distribution for a shop")
    public ResponseEntity<ApiResponse<ShopRatingResponse>> getShopRating(
            @PathVariable Long shopId) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.getShopRating(shopId)));
    }

    // ── Shop: view own reviews ────────────────────────────────────────────────
    @GetMapping("/api/shop/reviews/{shopId}")
    @Operation(summary = "Shop views all its own reviews")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getShopReviews(
            @PathVariable Long shopId) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.getVisibleReviews(shopId)));
    }

    // ── Admin: view all reviews including hidden ───────────────────────────────
    @GetMapping("/api/admin/reviews/{shopId}")
    @Operation(summary = "Admin views all reviews for a shop including hidden")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> adminGetReviews(
            @PathVariable Long shopId) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.getAllReviewsForAdmin(shopId)));
    }

    // ── Admin: hide/show a review ─────────────────────────────────────────────
    @PutMapping("/api/admin/reviews/{reviewId}/status")
    @Operation(summary = "Admin hides or shows a review")
    public ResponseEntity<ApiResponse<ReviewResponse>> setStatus(
            @PathVariable Long reviewId,
            @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.success(
                "Review status updated.", reviewService.setReviewStatus(reviewId, status)));
    }
}