package lk.goldvault.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.goldvault.backend.dto.response.ApiResponse;
import lk.goldvault.backend.dto.response.PawnTicketResponse;
import lk.goldvault.backend.service.PawnTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer/tickets")
@RequiredArgsConstructor
@Tag(name = "Customer Portal - Tickets", description = "Customer-facing read-only ticket views")
public class CustomerPawnTicketController {

    private final PawnTicketService pawnTicketService;

    @GetMapping("/{customerId}")
    @Operation(summary = "Get all tickets for the logged-in customer")
    public ResponseEntity<ApiResponse<List<PawnTicketResponse>>> myTickets(
            @PathVariable Long customerId) {
        return ResponseEntity.ok(ApiResponse.success(pawnTicketService.getByCustomer(customerId)));
    }

    @GetMapping("/detail/{ticketId}")
    @Operation(summary = "Get full detail of a single ticket, including QR code and outstanding balance")
    public ResponseEntity<ApiResponse<PawnTicketResponse>> ticketDetail(
            @PathVariable Long ticketId) {
        return ResponseEntity.ok(ApiResponse.success(pawnTicketService.getById(ticketId)));
    }
}