package lk.goldvault.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lk.goldvault.backend.dto.request.CustomerRequest;
import lk.goldvault.backend.dto.request.PaymentRequest;
import lk.goldvault.backend.dto.request.PawnTicketRequest;
import lk.goldvault.backend.dto.response.*;
import lk.goldvault.backend.entity.PawnShop;
import lk.goldvault.backend.service.CustomerService;
import lk.goldvault.backend.service.PawnTicketService;
import lk.goldvault.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * GoldVault Open API — v1
 *
 * Designed for enterprise pawn shops to integrate directly from their POS software.
 * Authentication: X-API-Key header (obtain from shop dashboard → API Keys section).
 *
 * Base URL: POST https://api.goldvault.lk/api/v1/pos/
 *
 * All responses follow the standard ApiResponse envelope:
 * { "success": true, "message": "...", "data": { ... } }
 */
@RestController
@RequestMapping("/api/v1/pos")
@RequiredArgsConstructor
@Tag(name = "POS Open API v1", description = "Enterprise POS integration API — authenticate with X-API-Key header")
public class PosApiController {

    private final CustomerService    customerService;
    private final PawnTicketService  pawnTicketService;
    private final PaymentService     paymentService;

    // ── Helper to extract shop from request attribute set by ApiKeyFilter ────────

    private PawnShop getShop(HttpServletRequest request) {
        PawnShop shop = (PawnShop) request.getAttribute("posShop");
        if (shop == null) throw new RuntimeException("Shop context missing — invalid API key.");
        return shop;
    }

    // ── Customers ────────────────────────────────────────────────────────────────

    @GetMapping("/customers")
    @Operation(
        summary     = "List all customers",
        description = "Returns all customers registered under your shop."
    )
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> listCustomers(
            HttpServletRequest request) {
        PawnShop shop = getShop(request);
        return ResponseEntity.ok(ApiResponse.success(
                customerService.getByShop(shop.getId())));
    }

    @GetMapping("/customers/nic/{nic}")
    @Operation(
        summary     = "Look up customer by NIC",
        description = "Find a customer by their NIC number. Use this before granting a ticket."
    )
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerByNic(
            @PathVariable String nic,
            HttpServletRequest request) {
        getShop(request); // validate key
        return ResponseEntity.ok(ApiResponse.success(
                customerService.getByNic(nic)));
    }

    @PostMapping("/customers")
    @Operation(
        summary     = "Register a new customer",
        description = "Creates a customer under your shop. NIC must be unique across the platform."
    )
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
            @Valid @RequestBody CustomerRequest body,
            HttpServletRequest request) {
        PawnShop shop = getShop(request);
        return ResponseEntity.ok(ApiResponse.success(
                "Customer registered.", customerService.register(shop.getId(), body)));
    }

    // ── Tickets ──────────────────────────────────────────────────────────────────

    @GetMapping("/tickets")
    @Operation(
        summary     = "List all tickets",
        description = "Returns all pawn tickets for your shop, newest first."
    )
    public ResponseEntity<ApiResponse<List<PawnTicketResponse>>> listTickets(
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        PawnShop shop = getShop(request);
        List<PawnTicketResponse> tickets = status != null && !status.isBlank()
                ? pawnTicketService.getByShopAndStatus(shop.getId(),
                        lk.goldvault.backend.enums.TicketStatus.valueOf(status.toUpperCase()))
                : pawnTicketService.getByShop(shop.getId());
        return ResponseEntity.ok(ApiResponse.success(tickets));
    }

    @GetMapping("/tickets/{id}")
    @Operation(
        summary     = "Get a single ticket",
        description = "Retrieve a ticket by its ID, including outstanding balance and gold items."
    )
    public ResponseEntity<ApiResponse<PawnTicketResponse>> getTicket(
            @PathVariable Long id,
            HttpServletRequest request) {
        getShop(request);
        return ResponseEntity.ok(ApiResponse.success(
                pawnTicketService.getById(id)));
    }

    @GetMapping("/tickets/number/{ticketNumber}")
    @Operation(
        summary     = "Get ticket by ticket number",
        description = "Look up a ticket by its GoldVault ticket number (e.g. GV-2026-000012)."
    )
    public ResponseEntity<ApiResponse<PawnTicketResponse>> getTicketByNumber(
            @PathVariable String ticketNumber,
            HttpServletRequest request) {
        getShop(request);
        return ResponseEntity.ok(ApiResponse.success(
                pawnTicketService.getByTicketNumber(ticketNumber)));
    }

    @PostMapping("/tickets")
    @Operation(
        summary     = "Grant a new pawn ticket",
        description = """
            Creates a new pawn ticket for a customer.

            Required fields:
            - customerId: ID of an existing customer under your shop
            - loanAmount: Loan amount in LKR
            - interestRate: Monthly interest rate as percentage (e.g. 2.5)
            - interestType: FLAT or REDUCING
            - periodMonths: Loan period in months (e.g. 6)
            - goldItems: Array with at least one item (description, goldType, weightGrams, purity)

            Returns the created ticket with its ticket number and QR code.
            """
    )
    public ResponseEntity<ApiResponse<PawnTicketResponse>> grantTicket(
            @Valid @RequestBody PawnTicketRequest body,
            HttpServletRequest request) {
        PawnShop shop = getShop(request);
        return ResponseEntity.ok(ApiResponse.success(
                "Ticket granted.", pawnTicketService.grantTicket(shop.getId(), body)));
    }

    @PutMapping("/tickets/{id}/redeem")
    @Operation(
        summary     = "Redeem a ticket",
        description = "Marks a ticket as REDEEMED. Customer has returned full outstanding balance."
    )
    public ResponseEntity<ApiResponse<PawnTicketResponse>> redeemTicket(
            @PathVariable Long id,
            HttpServletRequest request) {
        getShop(request);
        return ResponseEntity.ok(ApiResponse.success(
                "Ticket redeemed.", pawnTicketService.redeem(id)));
    }

    // ── Payments ─────────────────────────────────────────────────────────────────

    @PostMapping("/payments")
    @Operation(
        summary     = "Record a payment",
        description = """
            Records a payment against a ticket.

            paymentType options: INTEREST, PARTIAL, FULL_REDEMPTION, RENEWAL
            paymentMethod options: CASH, CARD, ONLINE_TRANSFER, LANKAQR
            """
    )
    public ResponseEntity<ApiResponse<PaymentResponse>> recordPayment(
            @Valid @RequestBody PaymentRequest body,
            HttpServletRequest request) {
        getShop(request);
        return ResponseEntity.ok(ApiResponse.success(
                "Payment recorded.", paymentService.recordPayment(body)));
    }

    @GetMapping("/tickets/{ticketId}/payments")
    @Operation(
        summary     = "Get payment history for a ticket",
        description = "Returns all payments recorded against a specific ticket."
    )
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPayments(
            @PathVariable Long ticketId,
            HttpServletRequest request) {
        getShop(request);
        return ResponseEntity.ok(ApiResponse.success(
                paymentService.getByTicket(ticketId)));
    }
}