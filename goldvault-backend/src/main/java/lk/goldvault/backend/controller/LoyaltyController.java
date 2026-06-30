package lk.goldvault.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.goldvault.backend.dto.request.RedeemPointsRequest;
import lk.goldvault.backend.dto.response.ApiResponse;
import lk.goldvault.backend.dto.response.LoyaltySummaryResponse;
import lk.goldvault.backend.service.LoyaltyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Loyalty", description = "Customer loyalty points")
public class LoyaltyController {

    private final LoyaltyService loyaltyService;

    @GetMapping("/api/customer/loyalty/{customerId}")
    @Operation(summary = "Get loyalty points summary and history for a customer")
    public ResponseEntity<ApiResponse<LoyaltySummaryResponse>> getSummary(
            @PathVariable Long customerId) {
        return ResponseEntity.ok(ApiResponse.success(loyaltyService.getSummary(customerId)));
    }

    @PostMapping("/api/shop/loyalty/redeem")
    @Operation(summary = "Redeem points for an interest discount (shop applies at grant-ticket time)")
    public ResponseEntity<ApiResponse<Double>> redeemPoints(
            @Valid @RequestBody RedeemPointsRequest request) {
        double discount = loyaltyService.redeemPoints(request);
        return ResponseEntity.ok(ApiResponse.success(
                "Discount of " + discount + "% applied.", discount));
    }
}