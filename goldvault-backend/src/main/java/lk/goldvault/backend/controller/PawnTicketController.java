package lk.goldvault.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.goldvault.backend.dto.request.PawnTicketRequest;
import lk.goldvault.backend.dto.response.ApiResponse;
import lk.goldvault.backend.dto.response.PawnTicketResponse;
import lk.goldvault.backend.enums.TicketStatus;
import lk.goldvault.backend.service.PawnTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lk.goldvault.backend.service.PawnTicketPdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;

@RestController
@RequestMapping("/api/shop/tickets")
@RequiredArgsConstructor
@Tag(name = "Pawn Ticket", description = "Grant, view, and manage pawn tickets")
public class PawnTicketController {

    private final PawnTicketService pawnTicketService;
    private final PawnTicketPdfService pawnTicketPdfService;

    @PostMapping("/{shopId}")
    @Operation(summary = "Grant a new pawn ticket", description = "Creates a ticket with one or more gold items and generates a QR code")
    public ResponseEntity<ApiResponse<PawnTicketResponse>> grantTicket(
            @PathVariable Long shopId,
            @Valid @RequestBody PawnTicketRequest request) {
        PawnTicketResponse response = pawnTicketService.grantTicket(shopId, request);
        return ResponseEntity.ok(ApiResponse.success("Ticket granted successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get ticket by internal id")
    public ResponseEntity<ApiResponse<PawnTicketResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(pawnTicketService.getById(id)));
    }

    @GetMapping("/number/{ticketNumber}")
    @Operation(summary = "Get ticket by ticket number (used by QR code scan lookup)")
    public ResponseEntity<ApiResponse<PawnTicketResponse>> getByTicketNumber(
            @PathVariable String ticketNumber) {
        return ResponseEntity.ok(ApiResponse.success(pawnTicketService.getByTicketNumber(ticketNumber)));
    }

    @GetMapping("/shop/{shopId}")
    @Operation(summary = "List all tickets for a shop")
    public ResponseEntity<ApiResponse<List<PawnTicketResponse>>> getByShop(@PathVariable Long shopId) {
        return ResponseEntity.ok(ApiResponse.success(pawnTicketService.getByShop(shopId)));
    }

    @GetMapping("/shop/{shopId}/status/{status}")
    @Operation(summary = "List tickets for a shop filtered by status")
    public ResponseEntity<ApiResponse<List<PawnTicketResponse>>> getByShopAndStatus(
            @PathVariable Long shopId,
            @PathVariable TicketStatus status) {
        return ResponseEntity.ok(ApiResponse.success(pawnTicketService.getByShopAndStatus(shopId, status)));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "List all tickets belonging to a customer")
    public ResponseEntity<ApiResponse<List<PawnTicketResponse>>> getByCustomer(
            @PathVariable Long customerId) {
        return ResponseEntity.ok(ApiResponse.success(pawnTicketService.getByCustomer(customerId)));
    }

    @PutMapping("/{id}/redeem")
    @Operation(summary = "Redeem (close) an active ticket")
    public ResponseEntity<ApiResponse<PawnTicketResponse>> redeem(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Ticket redeemed", pawnTicketService.redeem(id)));
    }
    @GetMapping("/{id}/receipt/pdf")
    @Operation(summary = "Download pawn ticket receipt as PDF")
    public ResponseEntity<byte[]> downloadReceiptPdf(@PathVariable Long id) {
        byte[] pdf = pawnTicketPdfService.generateReceiptPdf(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment",
                "pawn-receipt-" + id + ".pdf");
        headers.setContentLength(pdf.length);
        return ResponseEntity.ok().headers(headers).body(pdf);
    }
    
}