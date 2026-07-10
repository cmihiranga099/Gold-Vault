package lk.goldvault.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.goldvault.backend.dto.response.ApiResponse;
import lk.goldvault.backend.dto.response.PaymentResponse;
import lk.goldvault.backend.dto.response.PaymentSubmissionResponse;
import lk.goldvault.backend.enums.PaymentType;
import lk.goldvault.backend.service.PaymentService;
import lk.goldvault.backend.service.PaymentSubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/customer/payments")
@RequiredArgsConstructor
@Tag(name = "Customer Portal - Payments", description = "Customer-facing payment history and online repayment submissions")
public class CustomerPaymentController {

    private final PaymentService paymentService;
    private final PaymentSubmissionService paymentSubmissionService;

    @GetMapping("/ticket/{ticketId}")
    @Operation(summary = "View payment history for one of the customer's own tickets")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> paymentHistory(
            @PathVariable Long ticketId) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getByTicket(ticketId)));
    }

    @PostMapping(value = "/submissions/{customerId}", consumes = "multipart/form-data")
    @Operation(summary = "Submit an online repayment (bank transfer + receipt) for shop review")
    public ResponseEntity<ApiResponse<PaymentSubmissionResponse>> submit(
            @PathVariable Long customerId,
            @RequestParam Long ticketId,
            @RequestParam BigDecimal amount,
            @RequestParam PaymentType paymentType,
            @RequestParam(required = false) String bankName,
            @RequestParam String referenceNumber,
            @RequestParam("receipt") MultipartFile receipt) {
        PaymentSubmissionResponse response = paymentSubmissionService.submit(
                customerId, ticketId, amount, paymentType, bankName, referenceNumber, receipt);
        return ResponseEntity.ok(ApiResponse.success(
                "Payment submitted — the shop will confirm it shortly.", response));
    }

    @GetMapping("/submissions/ticket/{ticketId}")
    @Operation(summary = "View this ticket's online payment submissions and their review status")
    public ResponseEntity<ApiResponse<List<PaymentSubmissionResponse>>> submissionsForTicket(
            @PathVariable Long ticketId) {
        return ResponseEntity.ok(ApiResponse.success(paymentSubmissionService.getForTicket(ticketId)));
    }
}