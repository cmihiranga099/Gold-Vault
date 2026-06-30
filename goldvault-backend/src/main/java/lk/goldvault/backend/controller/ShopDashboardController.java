package lk.goldvault.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.goldvault.backend.dto.response.ApiResponse;
import lk.goldvault.backend.dto.response.ShopDashboardResponse;
import lk.goldvault.backend.service.ShopDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lk.goldvault.backend.service.ShopManagementService;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/shop/dashboard")
@RequiredArgsConstructor
@Tag(name = "Shop Dashboard", description = "Shop-scoped overview stats")
public class ShopDashboardController {

    private final ShopDashboardService shopDashboardService;

    @GetMapping("/{shopId}")
    @Operation(summary = "Get dashboard summary for a shop")
    public ResponseEntity<ApiResponse<ShopDashboardResponse>> getSummary(
            @PathVariable Long shopId) {
        return ResponseEntity.ok(ApiResponse.success(shopDashboardService.getSummary(shopId)));
    }
    @PutMapping("/{shopId}/location")
    @Operation(summary = "Update shop's map pin location (latitude/longitude)")
    public ResponseEntity<ApiResponse<ShopResponse>> updateLocation(
            @PathVariable Long shopId,
            @RequestParam BigDecimal latitude,
            @RequestParam BigDecimal longitude) {
        return ResponseEntity.ok(ApiResponse.success(
                "Location updated", shopManagementService.updateLocation(shopId, latitude, longitude)));
    }
}