package lk.goldvault.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.goldvault.backend.dto.request.CustomerRequest;
import lk.goldvault.backend.dto.response.ApiResponse;
import lk.goldvault.backend.dto.response.CustomerResponse;
import lk.goldvault.backend.dto.response.PagedResponse;
import lk.goldvault.backend.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shop/customers")
@RequiredArgsConstructor
@Tag(name = "Customer Management", description = "Shop-side customer registration and search")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/{shopId}")
    @Operation(summary = "Register a new customer under a shop")
    public ResponseEntity<ApiResponse<CustomerResponse>> register(
            @PathVariable Long shopId,
            @Valid @RequestBody CustomerRequest request) {
        CustomerResponse response = customerService.register(shopId, request);
        return ResponseEntity.ok(ApiResponse.success("Customer registered successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by internal id")
    public ResponseEntity<ApiResponse<CustomerResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getById(id)));
    }

    @GetMapping("/nic/{nic}")
    @Operation(summary = "Get customer by NIC number")
    public ResponseEntity<ApiResponse<CustomerResponse>> getByNic(@PathVariable String nic) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getByNic(nic)));
    }

    @GetMapping("/shop/{shopId}")
    @Operation(summary = "List all customers for a shop")
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getByShop(@PathVariable Long shopId) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getByShop(shopId)));
    }

    @GetMapping("/shop/{shopId}/search")
    @Operation(summary = "Search customers by name within a shop")
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> search(
            @PathVariable Long shopId,
            @RequestParam String name) {
        return ResponseEntity.ok(ApiResponse.success(customerService.searchByName(shopId, name)));
    }

    @GetMapping("/shop/{shopId}/paged")
    @Operation(summary = "Paginated customer listing for a shop",
            description = "Sorted by most recently created. page is 0-indexed.")
    public ResponseEntity<ApiResponse<PagedResponse<CustomerResponse>>> getByShopPaged(
            @PathVariable Long shopId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                customerService.getByShopPaged(shopId, page, size)));
    }

    @GetMapping("/shop/{shopId}/search/paged")
    @Operation(summary = "Paginated search by name or NIC within a shop",
            description = "Case-insensitive partial match on full name or NIC. page is 0-indexed.")
    public ResponseEntity<ApiResponse<PagedResponse<CustomerResponse>>> searchPaged(
            @PathVariable Long shopId,
            @RequestParam String term,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                customerService.searchPaged(shopId, term, page, size)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update customer details")
    public ResponseEntity<ApiResponse<CustomerResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Customer updated", customerService.update(id, request)));
    }
}