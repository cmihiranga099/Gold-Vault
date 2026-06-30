package lk.goldvault.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.goldvault.backend.dto.response.ApiResponse;
import lk.goldvault.backend.dto.response.CsvImportSummaryResponse;
import lk.goldvault.backend.service.BulkTicketImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/shop/tickets/bulk-import")
@RequiredArgsConstructor
@Tag(name = "Bulk Ticket Import", description = "CSV bulk import of existing pawn tickets")
public class BulkTicketImportController {

    private final BulkTicketImportService bulkTicketImportService;

    @PostMapping("/{shopId}")
    @Operation(summary = "Upload a CSV file to bulk-import pawn tickets")
    public ResponseEntity<ApiResponse<CsvImportSummaryResponse>> importCsv(
            @PathVariable Long shopId,
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            throw new RuntimeException("Uploaded file is empty.");
        }
        if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
            throw new RuntimeException("Only .csv files are accepted.");
        }

        CsvImportSummaryResponse summary = bulkTicketImportService.importTickets(shopId, file);

        String message = summary.getFailureCount() == 0
                ? "All " + summary.getSuccessCount() + " ticket(s) imported successfully."
                : summary.getSuccessCount() + " imported, " + summary.getFailureCount() + " failed. See row details.";

        return ResponseEntity.ok(ApiResponse.success(message, summary));
    }

    @GetMapping("/template")
    @Operation(summary = "Download a CSV template with correct headers and sample row")
    public ResponseEntity<ByteArrayResource> downloadTemplate() {
        String csv = """
                customerNic,customerName,customerPhone,loanAmount,interestRate,interestType,periodMonths,pawnDate,itemDescription,goldType,weightGrams,purity,estimatedValue,notes
                199012345678,W.M. Kamal Perera,0771234567,50000,2.5,FLAT,6,2026-01-15,22K gold chain,CHAIN,8.5,K22,55000,
                851234567V,Nimal Silva,0779876543,75000,3,REDUCING,3,2026-02-01,Gold bangle pair,BANGLE,15.2,K22,82000,Customer requested quick redemption
                """;

        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);
        ByteArrayResource resource = new ByteArrayResource(bytes);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "goldvault-ticket-import-template.csv");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(bytes.length)
                .body(resource);
    }
}