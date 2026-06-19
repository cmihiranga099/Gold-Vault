package lk.goldvault.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.goldvault.backend.dto.request.PaymentRequest;
import lk.goldvault.backend.dto.response.ApiResponse;
import lk.goldvault.backend.dto.response.DailyCollectionResponse;
import lk.goldvault.backend.dto.response.PaymentResponse;
import lk.goldvault.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/shop/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Record payments and view collection reports")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Record a payment against a ticket",
            description = "Automatically marks the ticket as REDEEMED if the payment clears the outstanding balance")
    public ResponseEntity<ApiResponse<PaymentResponse>> recordPayment(
            @Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.recordPayment(request);
        String message = response.isTicketRedeemed()
                ? "Payment recorded — ticket fully redeemed"
                : "Payment recorded successfully";
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    @GetMapping("/ticket/{ticketId}")
    @Operation(summary = "Get all payments made against a ticket")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getByTicket(
            @PathVariable Long ticketId) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getByTicket(ticketId)));
    }

    @GetMapping("/today")
    @Operation(summary = "Daily collection report for today")
    public ResponseEntity<ApiResponse<DailyCollectionResponse>> today() {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getDailyCollection(LocalDate.now())));
    }

    @GetMapping("/daily")
    @Operation(summary = "Daily collection report for a specific date (format: yyyy-MM-dd)")
    public ResponseEntity<ApiResponse<DailyCollectionResponse>> daily(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getDailyCollection(date)));
    }
}