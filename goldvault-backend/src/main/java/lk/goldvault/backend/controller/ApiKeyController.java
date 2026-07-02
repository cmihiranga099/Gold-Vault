package lk.goldvault.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.goldvault.backend.dto.response.ApiResponse;
import lk.goldvault.backend.service.ApiKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shop/api-keys")
@RequiredArgsConstructor
@Tag(name = "API Key Management", description = "Manage POS integration API keys for your shop")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    @PostMapping("/{shopId}/generate")
    @Operation(
        summary     = "Generate a new API key",
        description = "Returns the raw API key ONCE. Store it securely — it cannot be retrieved again."
    )
    public ResponseEntity<ApiResponse<Map<String, String>>> generate(
            @PathVariable Long shopId,
            @RequestParam(defaultValue = "POS Integration") String label) {

        ApiKeyService.GeneratedKey generated = apiKeyService.generateKey(shopId, label);

        return ResponseEntity.ok(ApiResponse.success(
                "API key generated. Copy it now — it will not be shown again.",
                Map.of(
                    "apiKey",    generated.rawKey(),
                    "keyPrefix", generated.prefix(),
                    "label",     generated.label()
                )
        ));
    }

    @GetMapping("/{shopId}")
    @Operation(summary = "List all API keys for a shop (prefixes only — raw keys not shown)")
    public ResponseEntity<ApiResponse<List<ApiKeyService.ApiKeyInfo>>> list(
            @PathVariable Long shopId) {
        return ResponseEntity.ok(ApiResponse.success(
                apiKeyService.listKeys(shopId)));
    }

    @DeleteMapping("/{shopId}/revoke/{keyId}")
    @Operation(summary = "Revoke an API key")
    public ResponseEntity<ApiResponse<String>> revoke(
            @PathVariable Long shopId,
            @PathVariable Long keyId) {
        apiKeyService.revokeKey(keyId, shopId);
        return ResponseEntity.ok(ApiResponse.success("API key revoked."));
    }
}