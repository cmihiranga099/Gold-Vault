package lk.goldvault.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.goldvault.backend.dto.response.ApiResponse;
import lk.goldvault.backend.dto.response.DashboardSummaryResponse;
import lk.goldvault.backend.dto.response.RevenueReportResponse;
import lk.goldvault.backend.service.DashboardService;
import lk.goldvault.backend.service.RevenueReportExcelService;
import lk.goldvault.backend.service.RevenueReportPdfService;
import lk.goldvault.backend.service.RevenueReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
    private final RevenueReportPdfService revenueReportPdfService;
    private final RevenueReportExcelService revenueReportExcelService;
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

    @GetMapping("/reports/revenue/pdf")
    @Operation(summary = "Download the revenue and shop-performance report as a PDF")
    public ResponseEntity<byte[]> revenuePdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        RevenueReportResponse report = revenueReportService.generateReport(startDate, endDate);
        byte[] pdf = revenueReportPdfService.generatePdf(report);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment",
                "revenue-report-" + startDate + "-to-" + endDate + ".pdf");
        headers.setContentLength(pdf.length);
        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    @GetMapping("/reports/revenue/excel")
    @Operation(summary = "Download the revenue and shop-performance report as an Excel workbook")
    public ResponseEntity<byte[]> revenueExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        RevenueReportResponse report = revenueReportService.generateReport(startDate, endDate);
        byte[] excel = revenueReportExcelService.generateExcel(report);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment",
                "revenue-report-" + startDate + "-to-" + endDate + ".xlsx");
        headers.setContentLength(excel.length);
        return ResponseEntity.ok().headers(headers).body(excel);
    }
}