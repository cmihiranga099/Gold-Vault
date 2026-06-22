package lk.goldvault.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.goldvault.backend.dto.request.BranchRequest;
import lk.goldvault.backend.dto.response.ApiResponse;
import lk.goldvault.backend.dto.response.BranchResponse;
import lk.goldvault.backend.service.BranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shop/branches")
@RequiredArgsConstructor
@Tag(name = "Branch Management", description = "CRUD for shop branches, for multi-branch pawn companies")
public class BranchController {

    private final BranchService branchService;

    @PostMapping("/{shopId}")
    @Operation(summary = "Create a new branch under a shop",
            description = "If isMain is true, any existing main branch for the shop is automatically demoted")
    public ResponseEntity<ApiResponse<BranchResponse>> create(
            @PathVariable Long shopId,
            @Valid @RequestBody BranchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Branch created successfully", branchService.create(shopId, request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get branch by id")
    public ResponseEntity<ApiResponse<BranchResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(branchService.getById(id)));
    }

    @GetMapping("/shop/{shopId}")
    @Operation(summary = "List all branches for a shop")
    public ResponseEntity<ApiResponse<List<BranchResponse>>> getByShop(@PathVariable Long shopId) {
        return ResponseEntity.ok(ApiResponse.success(branchService.getByShop(shopId)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a branch")
    public ResponseEntity<ApiResponse<BranchResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody BranchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Branch updated successfully", branchService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a branch", description = "The main branch cannot be deleted")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        branchService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Branch deleted successfully", null));
    }
}