package lk.goldvault.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.goldvault.backend.dto.response.ApiResponse;
import lk.goldvault.backend.dto.response.ShopAnalyticsResponse;
import lk.goldvault.backend.service.ShopAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shop/analytics")
@RequiredArgsConstructor
@Tag(name = "Shop Analytics", description = "Premium analytics dashboard for pawn shops")
public class ShopAnalyticsController {

    private final ShopAnalyticsService shopAnalyticsService;

    @GetMapping("/{shopId}")
    @Operation(summary = "Get full analytics for a shop")
    public ResponseEntity<ApiResponse<ShopAnalyticsResponse>> getAnalytics(
            @PathVariable Long shopId) {
        ShopAnalyticsResponse response = shopAnalyticsService.getAnalytics(shopId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}