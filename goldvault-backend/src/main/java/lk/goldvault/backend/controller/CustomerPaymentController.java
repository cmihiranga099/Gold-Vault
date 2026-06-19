package lk.goldvault.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.goldvault.backend.dto.response.ApiResponse;
import lk.goldvault.backend.dto.response.PaymentResponse;
import lk.goldvault.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customer/payments")
@RequiredArgsConstructor
@Tag(name = "Customer Portal - Payments", description = "Customer-facing payment history")
public class CustomerPaymentController {

    private final PaymentService paymentService;

    @GetMapping("/ticket/{ticketId}")
    @Operation(summary = "View payment history for one of the customer's own tickets")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> paymentHistory(
            @PathVariable Long ticketId) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getByTicket(ticketId)));
    }
}