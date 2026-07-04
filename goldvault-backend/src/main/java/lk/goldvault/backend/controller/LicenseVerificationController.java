package lk.goldvault.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.goldvault.backend.dto.response.ApiResponse;
import lk.goldvault.backend.dto.response.ShopResponse;
import lk.goldvault.backend.entity.PawnShop;
import lk.goldvault.backend.enums.ShopStatus;
import lk.goldvault.backend.repository.PawnShopRepository;
import lk.goldvault.backend.service.ShopManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/licenses")
@RequiredArgsConstructor
@Tag(name = "License Verification", description = "Admin: verify pawn broker license documents")
public class LicenseVerificationController {

    private final PawnShopRepository    pawnShopRepository;
    private final ShopManagementService shopManagementService;

    /** List all shops with PENDING license verification */
    @GetMapping("/pending")
    @Operation(summary = "List shops with pending license verification")
    public ResponseEntity<ApiResponse<List<ShopResponse>>> getPending() {
        List<ShopResponse> pending = shopManagementService.getAll().stream()
                .filter(s -> "PENDING".equals(s.getLicenseStatus()))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(pending));
    }

    /** List all shops with any license status */
    @GetMapping
    @Operation(summary = "List all shops with license info")
    public ResponseEntity<ApiResponse<List<ShopResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(shopManagementService.getAll()));
    }

    /** Admin verifies a shop's license */
    @PutMapping("/{shopId}/verify")
    @Operation(summary = "Mark a shop's license as VERIFIED")
    public ResponseEntity<ApiResponse<ShopResponse>> verify(
            @PathVariable Long shopId,
            @RequestParam(defaultValue = "Admin") String verifiedBy) {

        PawnShop shop = pawnShopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found: " + shopId));

        shop.setLicenseStatus("VERIFIED");
        shop.setLicenseVerifiedAt(LocalDateTime.now());
        shop.setLicenseVerifiedBy(verifiedBy);
        shop.setLicenseRejectReason(null);

        // Auto-activate shop once license is verified
        if (shop.getStatus() == ShopStatus.PENDING) {
            shop.setStatus(ShopStatus.ACTIVE);
        }

        pawnShopRepository.save(shop);
        return ResponseEntity.ok(ApiResponse.success(
                "License verified. Shop is now ACTIVE.", shopManagementService.getById(shopId)));
    }

    /** Admin rejects a shop's license */
    @PutMapping("/{shopId}/reject")
    @Operation(summary = "Reject a shop's license with a reason")
    public ResponseEntity<ApiResponse<ShopResponse>> reject(
            @PathVariable Long shopId,
            @RequestBody Map<String, String> body) {

        String reason     = body.getOrDefault("reason", "License document is invalid or incomplete.");
        String reviewedBy = body.getOrDefault("reviewedBy", "Admin");

        PawnShop shop = pawnShopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found: " + shopId));

        shop.setLicenseStatus("REJECTED");
        shop.setLicenseRejectReason(reason);
        shop.setLicenseVerifiedBy(reviewedBy);
        shop.setLicenseVerifiedAt(LocalDateTime.now());

        pawnShopRepository.save(shop);
        return ResponseEntity.ok(ApiResponse.success(
                "License rejected.", shopManagementService.getById(shopId)));
    }

    /** Shop uploads/re-uploads their license document */
    @PutMapping("/{shopId}/upload")
    @Operation(summary = "Link an uploaded license document to a shop")
    public ResponseEntity<ApiResponse<ShopResponse>> linkDocument(
            @PathVariable Long shopId,
            @RequestBody Map<String, String> body) {

        PawnShop shop = pawnShopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found: " + shopId));

        shop.setLicenseDocumentUrl(body.get("licenseDocumentUrl"));
        shop.setLicenseStatus("PENDING"); // Reset to pending for re-review
        shop.setLicenseRejectReason(null);

        pawnShopRepository.save(shop);
        return ResponseEntity.ok(ApiResponse.success(
                "License document submitted for review.", shopManagementService.getById(shopId)));
    }
}