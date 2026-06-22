package lk.goldvault.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.goldvault.backend.dto.request.GoldOfferRequest;
import lk.goldvault.backend.dto.response.ApiResponse;
import lk.goldvault.backend.dto.response.GoldOfferResponse;
import lk.goldvault.backend.service.GoldOfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shop/offers")
@RequiredArgsConstructor
@Tag(name = "Shop Gold Offers", description = "Shop submits offers on open customer listings")
public class ShopGoldOfferController {

    private final GoldOfferService goldOfferService;

    @PostMapping("/{shopId}")
    @Operation(summary = "Submit an offer on a customer's gold listing")
    public ResponseEntity<ApiResponse<GoldOfferResponse>> submitOffer(
            @PathVariable Long shopId,
            @Valid @RequestBody GoldOfferRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Offer submitted successfully", goldOfferService.submitOffer(shopId, request)));
    }

    @GetMapping("/shop/{shopId}")
    @Operation(summary = "Get all offers submitted by this shop")
    public ResponseEntity<ApiResponse<List<GoldOfferResponse>>> getByShop(
            @PathVariable Long shopId) {
        return ResponseEntity.ok(ApiResponse.success(goldOfferService.getByShop(shopId)));
    }

    @PutMapping("/{offerId}/withdraw")
    @Operation(summary = "Withdraw a pending offer")
    public ResponseEntity<ApiResponse<GoldOfferResponse>> withdraw(
            @PathVariable Long offerId,
            @RequestParam Long shopId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Offer withdrawn", goldOfferService.withdrawOffer(offerId, shopId)));
    }
}