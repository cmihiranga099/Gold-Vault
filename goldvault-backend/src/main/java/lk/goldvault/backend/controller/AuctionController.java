package lk.goldvault.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.goldvault.backend.dto.request.PlaceBidRequest;
import lk.goldvault.backend.dto.response.ApiResponse;
import lk.goldvault.backend.dto.response.AuctionBidResponse;
import lk.goldvault.backend.dto.response.AuctionResponse;
import lk.goldvault.backend.service.AuctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Auctions", description = "Auctions for expired pawn tickets")
public class AuctionController {

    private final AuctionService auctionService;

    // ── Public: browse + bid ───────────────────────────────────────────────────

    @GetMapping("/api/public/auctions")
    @Operation(summary = "List all open auctions")
    public ResponseEntity<ApiResponse<List<AuctionResponse>>> getOpenAuctions() {
        return ResponseEntity.ok(ApiResponse.success(auctionService.getOpenAuctions()));
    }

    @GetMapping("/api/public/auctions/{id}")
    @Operation(summary = "Get a single auction's details")
    public ResponseEntity<ApiResponse<AuctionResponse>> getAuction(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(auctionService.getById(id)));
    }

    @GetMapping("/api/public/auctions/{id}/bids")
    @Operation(summary = "Get bid history for an auction (bidder names masked)")
    public ResponseEntity<ApiResponse<List<AuctionBidResponse>>> getBids(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(auctionService.getBidHistory(id)));
    }

    @PostMapping("/api/public/auctions/{id}/bids")
    @Operation(summary = "Place a bid on an auction")
    public ResponseEntity<ApiResponse<AuctionResponse>> placeBid(
            @PathVariable Long id,
            @Valid @RequestBody PlaceBidRequest request) {
        AuctionResponse response = auctionService.placeBid(id, request);
        return ResponseEntity.ok(ApiResponse.success("Bid placed successfully.", response));
    }

    // ── Shop: manage auctions ──────────────────────────────────────────────────

    @GetMapping("/api/shop/auctions/{shopId}")
    @Operation(summary = "Shop views all its auctions")
    public ResponseEntity<ApiResponse<List<AuctionResponse>>> getShopAuctions(
            @PathVariable Long shopId) {
        return ResponseEntity.ok(ApiResponse.success(auctionService.getByShop(shopId)));
    }

    @PostMapping("/api/shop/auctions/tickets/{ticketId}/start")
    @Operation(summary = "Manually start an auction for an EXPIRED ticket")
    public ResponseEntity<ApiResponse<AuctionResponse>> startAuction(
            @PathVariable Long ticketId) {
        AuctionResponse response = auctionService.createAuctionManually(ticketId);
        return ResponseEntity.ok(ApiResponse.success("Auction started.", response));
    }

    @PutMapping("/api/shop/auctions/{id}/close")
    @Operation(summary = "Close an auction and determine the winner")
    public ResponseEntity<ApiResponse<AuctionResponse>> closeAuction(@PathVariable Long id) {
        AuctionResponse response = auctionService.closeAuction(id);
        return ResponseEntity.ok(ApiResponse.success("Auction closed.", response));
    }

    @PutMapping("/api/shop/auctions/{id}/cancel")
    @Operation(summary = "Cancel an auction")
    public ResponseEntity<ApiResponse<AuctionResponse>> cancelAuction(@PathVariable Long id) {
        AuctionResponse response = auctionService.cancelAuction(id);
        return ResponseEntity.ok(ApiResponse.success("Auction cancelled.", response));
    }
}