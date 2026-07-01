package lk.goldvault.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.goldvault.backend.dto.request.PromotionRequest;
import lk.goldvault.backend.dto.response.ApiResponse;
import lk.goldvault.backend.dto.response.PromotionResponse;
import lk.goldvault.backend.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Promotions", description = "Shop promotional campaigns")
public class PromotionController {

    private final PromotionService promotionService;

    // ── Shop: manage own promotions ───────────────────────────────────────────

    @PostMapping("/api/shop/promotions/{shopId}")
    @Operation(summary = "Create a new promotional campaign")
    public ResponseEntity<ApiResponse<PromotionResponse>> create(
            @PathVariable Long shopId,
            @Valid @RequestBody PromotionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Promotion created.", promotionService.create(shopId, request)));
    }

    @GetMapping("/api/shop/promotions/{shopId}")
    @Operation(summary = "Get all promotions for a shop (including past)")
    public ResponseEntity<ApiResponse<List<PromotionResponse>>> getByShop(
            @PathVariable Long shopId) {
        return ResponseEntity.ok(ApiResponse.success(
                promotionService.getByShop(shopId)));
    }

    @PutMapping("/api/shop/promotions/{promoId}/cancel/{shopId}")
    @Operation(summary = "Cancel a promotion")
    public ResponseEntity<ApiResponse<PromotionResponse>> cancel(
            @PathVariable Long promoId,
            @PathVariable Long shopId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Promotion cancelled.", promotionService.cancel(promoId, shopId)));
    }

    // ── Public: browse all active promotions ──────────────────────────────────

    @GetMapping("/api/public/promotions")
    @Operation(summary = "Get all currently active promotions across all shops")
    public ResponseEntity<ApiResponse<List<PromotionResponse>>> getAllActive() {
        return ResponseEntity.ok(ApiResponse.success(
                promotionService.getAllActivePromotions()));
    }

    @GetMapping("/api/public/promotions/shop/{shopId}")
    @Operation(summary = "Get active promotions for a specific shop")
    public ResponseEntity<ApiResponse<List<PromotionResponse>>> getActiveByShop(
            @PathVariable Long shopId) {
        return ResponseEntity.ok(ApiResponse.success(
                promotionService.getActiveByShop(shopId)));
    }
}