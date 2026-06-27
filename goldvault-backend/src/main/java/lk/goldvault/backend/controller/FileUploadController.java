package lk.goldvault.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.goldvault.backend.dto.response.ApiResponse;
import lk.goldvault.backend.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/shop/upload")
@RequiredArgsConstructor
@Tag(name = "File Upload", description = "Upload NIC photos and gold item photos")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    /**
     * Upload a NIC photo.
     * Returns: { "url": "uploads/nic/abc123.jpg" }
     */
    @PostMapping("/nic-photo")
    @Operation(summary = "Upload NIC photo for a customer")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadNicPhoto(
            @RequestParam("file") MultipartFile file) {
        String path = fileUploadService.save(file, "nic");
        return ResponseEntity.ok(ApiResponse.success("NIC photo uploaded", Map.of("url", path)));
    }

    /**
     * Upload a gold item photo.
     * Returns: { "url": "uploads/gold/abc123.jpg" }
     */
    @PostMapping("/gold-photo")
    @Operation(summary = "Upload gold item photo")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadGoldPhoto(
            @RequestParam("file") MultipartFile file) {
        String path = fileUploadService.save(file, "gold");
        return ResponseEntity.ok(ApiResponse.success("Gold photo uploaded", Map.of("url", path)));
    }
}