package lk.goldvault.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.goldvault.backend.dto.response.ApiResponse;
import lk.goldvault.backend.dto.response.GoldOfferResponse;
import lk.goldvault.backend.service.GoldOfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer/offers")
@RequiredArgsConstructor
@Tag(name = "Customer Portal - Offers", description = "Customer reviews and accepts/rejects offers on their listings")
public class CustomerGoldOfferController {

    private final GoldOfferService goldOfferService;

    @GetMapping("/listing/{listingId}")
    @Operation(summary = "Get all offers received on a specific listing")
    public ResponseEntity<ApiResponse<List<GoldOfferResponse>>> getByListing(
            @PathVariable Long listingId) {
        return ResponseEntity.ok(ApiResponse.success(goldOfferService.getByListing(listingId)));
    }

    @PutMapping("/{offerId}/accept")
    @Operation(summary = "Accept an offer",
            description = "Marks the listing as SOLD and rejects all other pending offers on it")
    public ResponseEntity<ApiResponse<GoldOfferResponse>> accept(
            @PathVariable Long offerId,
            @RequestParam Long customerId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Offer accepted", goldOfferService.acceptOffer(offerId, customerId)));
    }

    @PutMapping("/{offerId}/reject")
    @Operation(summary = "Reject an offer")
    public ResponseEntity<ApiResponse<GoldOfferResponse>> reject(
            @PathVariable Long offerId,
            @RequestParam Long customerId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Offer rejected", goldOfferService.rejectOffer(offerId, customerId)));
    }
}