package lk.goldvault.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.goldvault.backend.dto.response.ApiResponse;
import lk.goldvault.backend.dto.response.DashboardSummaryResponse;
import lk.goldvault.backend.dto.response.RevenueReportResponse;
import lk.goldvault.backend.service.DashboardService;
import lk.goldvault.backend.service.RevenueReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin - Reports", description = "Revenue analytics, commission tracking, and dashboard summary")
public class AdminReportController {

    private final RevenueReportService revenueReportService;
    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get overview summary stats for the admin dashboard")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> dashboard() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getSummary()));
    }

    @GetMapping("/reports/revenue")
    @Operation(summary = "Generate a revenue and commission report for a date range")
    public ResponseEntity<ApiResponse<RevenueReportResponse>> revenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.success(
                revenueReportService.generateReport(startDate, endDate)));
    }
}