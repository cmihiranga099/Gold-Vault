package lk.goldvault.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.goldvault.backend.dto.response.AdminUserResponse;
import lk.goldvault.backend.dto.response.ApiResponse;
import lk.goldvault.backend.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin - User Management", description = "Admin manages all platform users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    @Operation(summary = "List all users")
    public ResponseEntity<ApiResponse<List<AdminUserResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(adminUserService.getAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by id")
    public ResponseEntity<ApiResponse<AdminUserResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminUserService.getById(id)));
    }

    @PutMapping("/{id}/disable")
    @Operation(summary = "Disable a user account")
    public ResponseEntity<ApiResponse<AdminUserResponse>> disable(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("User disabled", adminUserService.disable(id)));
    }

    @PutMapping("/{id}/enable")
    @Operation(summary = "Enable a user account")
    public ResponseEntity<ApiResponse<AdminUserResponse>> enable(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("User enabled", adminUserService.enable(id)));
    }

    @PutMapping("/{id}/assign-staff")
    @Operation(summary = "Promote a user to STAFF role for a given shop")
    public ResponseEntity<ApiResponse<AdminUserResponse>> assignAsStaff(
            @PathVariable Long id,
            @RequestParam Long shopId) {
        return ResponseEntity.ok(ApiResponse.success(
                "User assigned as staff", adminUserService.assignAsStaff(id, shopId)));
    }
}