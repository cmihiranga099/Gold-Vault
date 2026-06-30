package lk.goldvault.backend.service;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class NicOcrService {

    @Value("${app.ocr.tessdata.path:}")
    private String tessdataPath;

    @Value("${app.ocr.enabled:true}")
    private boolean ocrEnabled;

    // Old NIC: 9 digits + V or X. New NIC: 12 digits.
    private static final Pattern OLD_NIC_PATTERN = Pattern.compile("\\b(\\d{9}[VvXx])\\b");
    private static final Pattern NEW_NIC_PATTERN = Pattern.compile("\\b(\\d{12})\\b");

    /**
     * Extracts NIC number, estimated DOB, and gender from an uploaded NIC photo.
     * Returns a best-effort result — OCR on ID cards is never 100% reliable,
     * so the frontend always lets staff review/edit before saving.
     */
    public NicOcrResult extractFromImage(File imageFile) {
        if (!ocrEnabled) {
            return NicOcrResult.builder()
                    .success(false)
                    .message("OCR is disabled on this server.")
                    .build();
        }

        try {
            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath(tessdataPath);
            tesseract.setLanguage("eng");
            tesseract.setPageSegMode(6); // Assume a single uniform block of text
            tesseract.setOcrEngineMode(1); // LSTM neural net mode — most accurate

            String rawText = tesseract.doOCR(imageFile);
            log.info("[NIC OCR] Raw extracted text:\n{}", rawText);

            return parseNicText(rawText);

        } catch (TesseractException e) {
            log.error("[NIC OCR] OCR failed: {}", e.getMessage());
            return NicOcrResult.builder()
                    .success(false)
                    .message("Could not read the image. Please try a clearer photo.")
                    .build();
        }
    }

    // ── Parsing ──────────────────────────────────────────────────────────────────

    private NicOcrResult parseNicText(String rawText) {
        String cleaned = rawText.replaceAll("[\\n\\r]+", " ").trim();

        String nic = extractNic(cleaned);
        if (nic == null) {
            return NicOcrResult.builder()
                    .success(false)
                    .message("Could not detect a valid NIC number in the image. Please enter manually.")
                    .rawText(cleaned)
                    .build();
        }

        LocalDate dob = null;
        String gender = null;
        try {
            NicDetails details = decodeNic(nic);
            dob = details.dob;
            gender = details.gender;
        } catch (Exception e) {
            log.warn("[NIC OCR] Could not decode DOB/gender from NIC: {}", nic);
        }

        return NicOcrResult.builder()
                .success(true)
                .nic(nic.toUpperCase())
                .dob(dob)
                .gender(gender)
                .rawText(cleaned)
                .message("NIC detected. Please verify the details before saving.")
                .build();
    }

    private String extractNic(String text) {
        Matcher newMatcher = NEW_NIC_PATTERN.matcher(text);
        if (newMatcher.find()) return newMatcher.group(1);

        Matcher oldMatcher = OLD_NIC_PATTERN.matcher(text);
        if (oldMatcher.find()) return oldMatcher.group(1);

        return null;
    }

    /**
     * Decodes date of birth and gender from a Sri Lankan NIC number.
     * Old format (9 digits + V/X): YY DDD XXX V  — DDD is day-of-year (+500 if female)
     * New format (12 digits):      YYYY DDD XXXXX — DDD is day-of-year (+500 if female)
     */
    private NicDetails decodeNic(String nic) {
        String digits = nic.replaceAll("[VvXx]", "");
        int year;
        int dayOfYear;

        if (digits.length() == 9) {
            // Old format
            int yy = Integer.parseInt(digits.substring(0, 2));
            year = 1900 + yy;
            dayOfYear = Integer.parseInt(digits.substring(2, 5));
        } else if (digits.length() == 12) {
            // New format
            year = Integer.parseInt(digits.substring(0, 4));
            dayOfYear = Integer.parseInt(digits.substring(4, 7));
        } else {
            throw new IllegalArgumentException("Unrecognised NIC digit length");
        }

        String gender = "MALE";
        if (dayOfYear > 500) {
            gender = "FEMALE";
            dayOfYear -= 500;
        }

        // dayOfYear can occasionally be 1-366; clamp defensively
        if (dayOfYear < 1) dayOfYear = 1;
        if (dayOfYear > 366) dayOfYear = 366;

        LocalDate dob;
        try {
            dob = LocalDate.ofYearDay(year, dayOfYear);
        } catch (Exception e) {
            dob = LocalDate.of(year, 1, 1); // fallback if leap-year edge case fails
        }

        return new NicDetails(dob, gender);
    }

    private record NicDetails(LocalDate dob, String gender) {}

    // ── Result DTO ──────────────────────────────────────────────────────────────

    @lombok.Getter
    @lombok.Setter
    @lombok.Builder
    public static class NicOcrResult {
        private boolean success;
        private String  nic;
        private LocalDate dob;
        private String  gender;
        private String  message;
        private String  rawText;
    }
}