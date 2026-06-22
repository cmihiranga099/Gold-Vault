package lk.goldvault.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.goldvault.backend.dto.request.GoldListingRequest;
import lk.goldvault.backend.dto.response.ApiResponse;
import lk.goldvault.backend.dto.response.GoldListingResponse;
import lk.goldvault.backend.service.GoldListingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer/listings")
@RequiredArgsConstructor
@Tag(name = "Customer Portal - Gold Listings", description = "Customer creates and manages gold-for-sale listings")
public class CustomerGoldListingController {

    private final GoldListingService goldListingService;

    @PostMapping("/{customerId}")
    @Operation(summary = "Create a new gold listing for sale")
    public ResponseEntity<ApiResponse<GoldListingResponse>> create(
            @PathVariable Long customerId,
            @Valid @RequestBody GoldListingRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Listing created successfully", goldListingService.createListing(customerId, request)));
    }

    @GetMapping("/{customerId}")
    @Operation(summary = "Get all listings created by this customer")
    public ResponseEntity<ApiResponse<List<GoldListingResponse>>> myListings(
            @PathVariable Long customerId) {
        return ResponseEntity.ok(ApiResponse.success(goldListingService.getByCustomer(customerId)));
    }

    @GetMapping("/detail/{listingId}")
    @Operation(summary = "Get full detail of a listing, including all offers received")
    public ResponseEntity<ApiResponse<GoldListingResponse>> detail(
            @PathVariable Long listingId) {
        return ResponseEntity.ok(ApiResponse.success(goldListingService.getById(listingId)));
    }

    @PutMapping("/{listingId}/withdraw")
    @Operation(summary = "Withdraw a listing from the marketplace")
    public ResponseEntity<ApiResponse<GoldListingResponse>> withdraw(
            @PathVariable Long listingId,
            @RequestParam Long customerId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Listing withdrawn", goldListingService.withdraw(listingId, customerId)));
    }
}