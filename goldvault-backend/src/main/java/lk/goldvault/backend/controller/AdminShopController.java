package lk.goldvault.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.goldvault.backend.dto.response.ApiResponse;
import lk.goldvault.backend.dto.response.ShopResponse;
import lk.goldvault.backend.enums.ShopStatus;
import lk.goldvault.backend.service.ShopManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/shops")
@RequiredArgsConstructor
@Tag(name = "Admin - Shop Management", description = "Admin approves, suspends, and manages all shops")
public class AdminShopController {

    private final ShopManagementService shopManagementService;

    @GetMapping
    @Operation(summary = "List all shops")
    public ResponseEntity<ApiResponse<List<ShopResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(shopManagementService.getAll()));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "List shops filtered by status")
    public ResponseEntity<ApiResponse<List<ShopResponse>>> getByStatus(
            @PathVariable ShopStatus status) {
        return ResponseEntity.ok(ApiResponse.success(shopManagementService.getByStatus(status)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get shop by id")
    public ResponseEntity<ApiResponse<ShopResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(shopManagementService.getById(id)));
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve a pending shop")
    public ResponseEntity<ApiResponse<ShopResponse>> approve(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Shop approved", shopManagementService.approve(id)));
    }

    @PutMapping("/{id}/suspend")
    @Operation(summary = "Suspend an active shop")
    public ResponseEntity<ApiResponse<ShopResponse>> suspend(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Shop suspended", shopManagementService.suspend(id)));
    }

    @PutMapping("/{id}/reactivate")
    @Operation(summary = "Reactivate a suspended shop")
    public ResponseEntity<ApiResponse<ShopResponse>> reactivate(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Shop reactivated", shopManagementService.reactivate(id)));
    }
}