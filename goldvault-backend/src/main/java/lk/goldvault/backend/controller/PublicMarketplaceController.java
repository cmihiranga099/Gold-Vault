package lk.goldvault.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.goldvault.backend.dto.response.ApiResponse;
import lk.goldvault.backend.dto.response.GoldListingResponse;
import lk.goldvault.backend.dto.response.GoldRateResponse;
import lk.goldvault.backend.service.GoldListingService;
import lk.goldvault.backend.service.GoldRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/marketplace")
@RequiredArgsConstructor
@Tag(name = "Public Marketplace", description = "Public, no-login rate comparison and open listings")
public class PublicMarketplaceController {

    private final GoldRateService goldRateService;
    private final GoldListingService goldListingService;

    @GetMapping("/rates")
    @Operation(summary = "Compare today's active gold buying rates across all shops")
    public ResponseEntity<ApiResponse<List<GoldRateResponse>>> compareRates() {
        return ResponseEntity.ok(ApiResponse.success(goldRateService.getAllActiveRates()));
    }

    @GetMapping("/listings")
    @Operation(summary = "Browse all open gold listings (visible to shops looking to make offers)")
    public ResponseEntity<ApiResponse<List<GoldListingResponse>>> openListings() {
        return ResponseEntity.ok(ApiResponse.success(goldListingService.getOpenListings()));
    }
}