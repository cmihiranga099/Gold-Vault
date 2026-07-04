package lk.goldvault.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class FileUploadService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp",
            "application/pdf"
    );
    private static final long MAX_BYTES = 5 * 1024 * 1024; // 5 MB

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /**
     * Saves a multipart file into uploads/{subfolder}/ and returns the relative URL path.
     * e.g. "uploads/nic/abc123.jpg"
     */
    public String save(MultipartFile file, String subfolder) {
        validateFile(file);

        try {
            Path dir = Paths.get(uploadDir, subfolder);
            Files.createDirectories(dir);

            String ext      = getExtension(file.getOriginalFilename());
            String filename = UUID.randomUUID() + "." + ext;
            Path   target   = dir.resolve(filename);

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            return uploadDir + "/" + subfolder + "/" + filename;

        } catch (IOException e) {
            throw new RuntimeException("Could not save file: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a previously saved file given its relative path.
     * Silent no-op if file does not exist.
     */
    public void delete(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) return;
        try {
            Files.deleteIfExists(Paths.get(relativePath));
        } catch (IOException ignored) {}
    }

    // ── Validation ─────────────────────────────────────────────────────────────

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty.");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new RuntimeException("Only JPEG, PNG, and WEBP images are allowed.");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new RuntimeException("File size must not exceed 5 MB.");
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "jpg";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}