package lk.goldvault.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.goldvault.backend.dto.request.PaymentRequest;
import lk.goldvault.backend.dto.request.RejectPaymentSubmissionRequest;
import lk.goldvault.backend.dto.response.ApiResponse;
import lk.goldvault.backend.dto.response.DailyCollectionResponse;
import lk.goldvault.backend.dto.response.PaymentResponse;
import lk.goldvault.backend.dto.response.PaymentSubmissionResponse;
import lk.goldvault.backend.service.PaymentService;
import lk.goldvault.backend.service.PaymentSubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/shop/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Record payments, review online repayment submissions, and view collection reports")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentSubmissionService paymentSubmissionService;

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

    // ── Online repayment submissions (bank transfer + receipt) ─────────────────────

    @GetMapping("/submissions/pending/{shopId}")
    @Operation(summary = "List this shop's pending online payment submissions awaiting review")
    public ResponseEntity<ApiResponse<List<PaymentSubmissionResponse>>> pendingSubmissions(
            @PathVariable Long shopId) {
        return ResponseEntity.ok(ApiResponse.success(paymentSubmissionService.getPendingForShop(shopId)));
    }

    @GetMapping("/submissions/ticket/{ticketId}")
    @Operation(summary = "View a ticket's online payment submissions and their review status")
    public ResponseEntity<ApiResponse<List<PaymentSubmissionResponse>>> submissionsForTicket(
            @PathVariable Long ticketId) {
        return ResponseEntity.ok(ApiResponse.success(paymentSubmissionService.getForTicket(ticketId)));
    }

    @PostMapping("/submissions/{id}/approve")
    @Operation(summary = "Approve a pending online payment submission",
            description = "Books a real payment against the ticket exactly as a cash payment would, and notifies the customer")
    public ResponseEntity<ApiResponse<PaymentSubmissionResponse>> approveSubmission(
            @PathVariable Long id,
            @RequestParam(required = false) Long reviewedBy) {
        return ResponseEntity.ok(ApiResponse.success(
                "Payment approved.", paymentSubmissionService.approve(id, reviewedBy)));
    }

    @PostMapping("/submissions/{id}/reject")
    @Operation(summary = "Reject a pending online payment submission with a reason")
    public ResponseEntity<ApiResponse<PaymentSubmissionResponse>> rejectSubmission(
            @PathVariable Long id,
            @RequestParam(required = false) Long reviewedBy,
            @Valid @RequestBody RejectPaymentSubmissionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Payment rejected.", paymentSubmissionService.reject(id, reviewedBy, request.getReason())));
    }
}