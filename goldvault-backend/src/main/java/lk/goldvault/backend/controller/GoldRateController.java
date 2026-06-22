package lk.goldvault.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.goldvault.backend.dto.request.GoldRateRequest;
import lk.goldvault.backend.dto.response.ApiResponse;
import lk.goldvault.backend.dto.response.GoldRateResponse;
import lk.goldvault.backend.service.GoldRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shop/gold-rates")
@RequiredArgsConstructor
@Tag(name = "Gold Rates", description = "Shop publishes and manages its gold buying rates")
public class GoldRateController {

    private final GoldRateService goldRateService;

    @PostMapping("/{shopId}")
    @Operation(summary = "Publish or update today's rate for a purity",
            description = "Deactivates any previous active rate for the same purity before creating the new one")
    public ResponseEntity<ApiResponse<GoldRateResponse>> publishRate(
            @PathVariable Long shopId,
            @Valid @RequestBody GoldRateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Rate published successfully", goldRateService.publishRate(shopId, request)));
    }

    @GetMapping("/{shopId}")
    @Operation(summary = "Get all active rates for a shop")
    public ResponseEntity<ApiResponse<List<GoldRateResponse>>> getActiveRates(
            @PathVariable Long shopId) {
        return ResponseEntity.ok(ApiResponse.success(goldRateService.getActiveRatesByShop(shopId)));
    }
}