package lk.goldvault.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.goldvault.backend.dto.request.AmlReviewRequest;
import lk.goldvault.backend.dto.response.ApiResponse;
import lk.goldvault.backend.dto.response.AmlFlagResponse;
import lk.goldvault.backend.service.AmlDetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/aml")
@RequiredArgsConstructor
@Tag(name = "AML", description = "Anti-Money Laundering flag management — Admin only")
public class AmlController {

    private final AmlDetectionService amlDetectionService;

    @GetMapping("/flags")
    @Operation(summary = "Get all AML flags (newest first)")
    public ResponseEntity<ApiResponse<List<AmlFlagResponse>>> getAllFlags(
            @RequestParam(defaultValue = "false") boolean openOnly) {
        var flags = openOnly
                ? amlDetectionService.getOpenFlags()
                : amlDetectionService.getAllFlags();
        return ResponseEntity.ok(ApiResponse.success(flags));
    }

    @GetMapping("/flags/customer/{customerId}")
    @Operation(summary = "Get all AML flags for a specific customer")
    public ResponseEntity<ApiResponse<List<AmlFlagResponse>>> getByCustomer(
            @PathVariable Long customerId) {
        return ResponseEntity.ok(ApiResponse.success(
                amlDetectionService.getFlagsByCustomer(customerId)));
    }

    @PutMapping("/flags/{flagId}/review")
    @Operation(summary = "Mark an AML flag as REVIEWED or DISMISSED")
    public ResponseEntity<ApiResponse<AmlFlagResponse>> reviewFlag(
            @PathVariable Long flagId,
            @Valid @RequestBody AmlReviewRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Flag updated.", amlDetectionService.reviewFlag(flagId, request)));
    }

    @PostMapping("/scan")
    @Operation(summary = "Manually trigger AML scan (admin use, testing)")
    public ResponseEntity<ApiResponse<String>> triggerScan() {
        int flags = amlDetectionService.triggerScan();
        return ResponseEntity.ok(ApiResponse.success(
                flags + " new AML flag(s) raised."));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get AML flag summary statistics")
    public ResponseEntity<ApiResponse<AmlDetectionService.AmlSummary>> getSummary() {
        return ResponseEntity.ok(ApiResponse.success(
                amlDetectionService.getSummary()));
    }
}