package lk.goldvault.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.goldvault.backend.dto.request.ShopRegistrationRequest;
import lk.goldvault.backend.dto.response.ApiResponse;
import lk.goldvault.backend.dto.response.ShopProfileResponse;
import lk.goldvault.backend.dto.response.ShopResponse;
import lk.goldvault.backend.entity.GoldRate;
import lk.goldvault.backend.entity.PawnShop;
import lk.goldvault.backend.enums.ShopStatus;
import lk.goldvault.backend.enums.TicketStatus;
import lk.goldvault.backend.repository.*;
import lk.goldvault.backend.service.PromotionService;
import lk.goldvault.backend.service.ReviewService;
import lk.goldvault.backend.service.ShopManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
@Tag(name = "Shop", description = "Public shop endpoints")
public class PublicShopController {

    private final ShopManagementService  shopManagementService;
    private final PawnShopRepository     pawnShopRepository;
    private final GoldRateRepository     goldRateRepository;
    private final ShopReviewRepository   shopReviewRepository;
    private final PromotionRepository    promotionRepository;
    private final PawnTicketRepository   pawnTicketRepository;
    private final ReviewService          reviewService;
    private final PromotionService       promotionService;

    @PostMapping("/register")
    @Operation(summary = "Register a new shop")
    public ResponseEntity<ApiResponse<ShopResponse>> register(
            @Valid @RequestBody ShopRegistrationRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Shop registered successfully. Awaiting admin approval.",
                shopManagementService.register(request)));
    }

    @GetMapping("/active")
    @Operation(summary = "List all active shops")
    public ResponseEntity<ApiResponse<List<ShopResponse>>> getActiveShops() {
        return ResponseEntity.ok(ApiResponse.success(
                shopManagementService.getByStatus(ShopStatus.ACTIVE)));
    }

    @GetMapping("/{id}/profile")
    @Operation(summary = "Get a shop's full public profile")
    public ResponseEntity<ApiResponse<ShopProfileResponse>> getProfile(
            @PathVariable Long id) {
        PawnShop shop = pawnShopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shop not found: " + id));

        // Gold rates (purity → rate per gram)
        Map<String, BigDecimal> goldRates = goldRateRepository
                .findByShopIdAndIsActiveTrue(id)
                .stream()
                .collect(Collectors.toMap(
                        r -> r.getPurity().name(),
                        GoldRate::getRatePerGram,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        // Rating summary
        Double avg   = shopReviewRepository.averageRatingByShop(id);
        long   count = shopReviewRepository.countByShopIdAndStatus(id, "VISIBLE");
        Map<Integer, Long> dist = new LinkedHashMap<>();
        for (int i = 5; i >= 1; i--) dist.put(i, 0L);
        shopReviewRepository.ratingDistributionByShop(id)
                .forEach(row -> dist.put(((Number) row[0]).intValue(),
                        ((Number) row[1]).longValue()));

        // Recent 5 visible reviews
        var recentReviews = reviewService.getVisibleReviews(id)
                .stream().limit(5).toList();

        // Active promotions
        var activePromos = promotionService.getActiveByShop(id);

        // Ticket stats
        long totalTickets  = pawnTicketRepository.findByShopId(id).size();
        long activeTickets = pawnTicketRepository
                .findByShopIdAndStatus(id, TicketStatus.ACTIVE).size();

        ShopProfileResponse profile = ShopProfileResponse.builder()
                .id(shop.getId())
                .name(shop.getName())
                .ownerName(shop.getOwnerName())
                .phone(shop.getPhone())
                .email(shop.getEmail())
                .address(shop.getAddress())
                .latitude(shop.getLatitude())
                .longitude(shop.getLongitude())
                .status(shop.getStatus().name())
                .goldRates(goldRates)
                .averageRating(avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0)
                .totalReviews(count)
                .ratingDistribution(dist)
                .recentReviews(recentReviews)
                .activePromotions(activePromos)
                .totalTicketsEver(totalTickets)
                .activeTickets(activeTickets)
                .build();

        return ResponseEntity.ok(ApiResponse.success(profile));
    }
}