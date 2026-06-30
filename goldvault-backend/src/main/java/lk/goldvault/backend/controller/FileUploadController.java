package lk.goldvault.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.goldvault.backend.dto.response.ApiResponse;
import lk.goldvault.backend.service.FileUploadService;
import lk.goldvault.backend.service.NicOcrService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/shop/upload")
@RequiredArgsConstructor
@Tag(name = "File Upload", description = "Upload NIC photos and gold item photos")
public class FileUploadController {

    private final FileUploadService fileUploadService;
    private final NicOcrService     nicOcrService;

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

    /**
     * Upload a NIC photo AND run OCR on it in one step.
     * Returns the saved photo URL plus extracted NIC/DOB/gender if detected.
     */
    @PostMapping("/nic-photo-ocr")
    @Operation(summary = "Upload NIC photo and extract NIC number via OCR")
    public ResponseEntity<ApiResponse<NicOcrUploadResponse>> uploadAndScanNic(
            @RequestParam("file") MultipartFile file) {

        // Save the photo permanently first (same as the regular upload flow)
        String savedPath = fileUploadService.save(file, "nic");

        // Run OCR on a temp copy of the uploaded bytes
        NicOcrService.NicOcrResult ocrResult;
        File tempFile = null;
        try {
            tempFile = File.createTempFile("nic_ocr_", "_" + UUID.randomUUID() + ".jpg");
            file.transferTo(tempFile);
            ocrResult = nicOcrService.extractFromImage(tempFile);
        } catch (IOException e) {
            ocrResult = NicOcrService.NicOcrResult.builder()
                    .success(false)
                    .message("Could not process the image for OCR.")
                    .build();
        } finally {
            if (tempFile != null) {
                try { Files.deleteIfExists(tempFile.toPath()); } catch (IOException ignored) {}
            }
        }

        NicOcrUploadResponse response = NicOcrUploadResponse.builder()
                .photoUrl(savedPath)
                .ocrSuccess(ocrResult.isSuccess())
                .nic(ocrResult.getNic())
                .dob(ocrResult.getDob())
                .gender(ocrResult.getGender())
                .message(ocrResult.getMessage())
                .build();

        return ResponseEntity.ok(ApiResponse.success("NIC photo processed", response));
    }

    @lombok.Getter
    @lombok.Setter
    @lombok.Builder
    public static class NicOcrUploadResponse {
        private String photoUrl;
        private boolean ocrSuccess;
        private String  nic;
        private java.time.LocalDate dob;
        private String  gender;
        private String  message;
    }
}