package lk.goldvault.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.goldvault.backend.dto.request.ShopRegistrationRequest;
import lk.goldvault.backend.dto.response.ApiResponse;
import lk.goldvault.backend.dto.response.ShopResponse;
import lk.goldvault.backend.service.ShopManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
@Tag(name = "Shop Registration", description = "Public self-registration for new pawn shops (pending admin approval)")
public class PublicShopController {

    private final ShopManagementService shopManagementService;

    @PostMapping("/register")
    @Operation(summary = "Register a new shop", description = "Shop starts in PENDING status until an admin approves it")
    public ResponseEntity<ApiResponse<ShopResponse>> register(
            @Valid @RequestBody ShopRegistrationRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Shop registered successfully. Awaiting admin approval.",
                shopManagementService.register(request)));
    }
}