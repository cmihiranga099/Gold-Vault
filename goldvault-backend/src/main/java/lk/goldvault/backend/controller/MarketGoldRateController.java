package lk.goldvault.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.goldvault.backend.dto.request.MarketRateRequest;
import lk.goldvault.backend.dto.response.ApiResponse;
import lk.goldvault.backend.dto.response.MarketRateResponse;
import lk.goldvault.backend.dto.response.RateComparisonResponse;
import lk.goldvault.backend.service.MarketGoldRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Market Gold Rate", description = "Platform-wide gold market reference rates")
public class MarketGoldRateController {

    private final MarketGoldRateService marketGoldRateService;

    // ── Admin: publish today's market rate ───────────────────────────────────────
    @PostMapping("/api/admin/market-rates")
    @Operation(summary = "Admin publishes today's market gold reference rate")
    public ResponseEntity<ApiResponse<MarketRateResponse>> publishRate(
            @Valid @RequestBody MarketRateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Market rate published.", marketGoldRateService.publishMarketRate(request)));
    }

    // ── Public: latest rates for all purities ────────────────────────────────────
    @GetMapping("/api/public/market-rates")
    @Operation(summary = "Get latest market reference rates for all purities")
    public ResponseEntity<ApiResponse<List<MarketRateResponse>>> getLatest() {
        return ResponseEntity.ok(ApiResponse.success(
                marketGoldRateService.getLatestMarketRates()));
    }

    // ── Public: 30-day history for chart ─────────────────────────────────────────
    @GetMapping("/api/public/market-rates/{purity}/history")
    @Operation(summary = "Get 30-day market rate history for a purity (for chart)")
    public ResponseEntity<ApiResponse<List<MarketRateResponse>>> getHistory(
            @PathVariable String purity) {
        return ResponseEntity.ok(ApiResponse.success(
                marketGoldRateService.getRateHistory(purity)));
    }

    // ── Public: compare all shops' rates vs market rate ──────────────────────────
    @GetMapping("/api/public/market-rates/{purity}/compare")
    @Operation(summary = "Compare all shops' rates vs the market reference rate")
    public ResponseEntity<ApiResponse<RateComparisonResponse>> compare(
            @PathVariable String purity) {
        return ResponseEntity.ok(ApiResponse.success(
                marketGoldRateService.getComparisonForPurity(purity)));
    }

    // ── Shop: compare their own rate vs market ────────────────────────────────────
    @GetMapping("/api/shop/market-rates/{purity}/compare")
    @Operation(summary = "Shop compares its own rate vs market rate for a purity")
    public ResponseEntity<ApiResponse<RateComparisonResponse>> shopCompare(
            @PathVariable String purity) {
        return ResponseEntity.ok(ApiResponse.success(
                marketGoldRateService.getComparisonForPurity(purity)));
    }
}