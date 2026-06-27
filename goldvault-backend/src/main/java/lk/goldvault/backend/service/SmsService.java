package lk.goldvault.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
@Slf4j
public class SmsService {

    @Value("${app.sms.enabled:false}")
    private boolean enabled;

    @Value("${app.sms.notify-lk.user-id:}")
    private String userId;

    @Value("${app.sms.notify-lk.api-key:}")
    private String apiKey;

    @Value("${app.sms.sender-id:GoldVault}")
    private String senderId;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * Send an SMS via Notify.lk.
     *
     * @param phone   Sri Lanka phone number — accepts 07XXXXXXXX or +947XXXXXXXX
     * @param message SMS body text (max 160 chars for single SMS)
     * @return true if sent successfully
     */
    public boolean send(String phone, String message) {
        if (!enabled) {
            // Log only — useful during development so you can see what would be sent
            log.info("[SMS MOCK] To: {} | Message: {}", phone, message);
            return true;
        }

        if (phone == null || phone.isBlank()) {
            log.warn("[SMS] Skipped — phone number is blank.");
            return false;
        }

        String normalised = normalisePhone(phone);
        if (normalised == null) {
            log.warn("[SMS] Skipped — could not normalise phone: {}", phone);
            return false;
        }

        try {
            // Notify.lk API: GET https://app.notify.lk/api/v1/send
            String url = UriComponentsBuilder
                    .fromHttpUrl("https://app.notify.lk/api/v1/send")
                    .queryParam("user_id",  userId)
                    .queryParam("api_key",  apiKey)
                    .queryParam("sender_id", senderId)
                    .queryParam("to",       normalised)
                    .queryParam("message",  message)
                    .toUriString();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                log.info("[SMS] Sent to {} | Status: {}", normalised, response.body());
                return true;
            } else {
                log.error("[SMS] Failed to send to {} | HTTP {} | Body: {}",
                        normalised, response.statusCode(), response.body());
                return false;
            }

        } catch (Exception e) {
            log.error("[SMS] Exception sending to {}: {}", normalised, e.getMessage());
            return false;
        }
    }

    /**
     * Normalise Sri Lanka phone numbers to international format (947XXXXXXXX).
     * Accepts: 0771234567 → 94771234567
     *          +94771234567 → 94771234567
     *          771234567 → 94771234567
     */
    private String normalisePhone(String raw) {
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.startsWith("94") && digits.length() == 11) return digits;
        if (digits.startsWith("0")  && digits.length() == 10) return "94" + digits.substring(1);
        if (digits.length() == 9) return "94" + digits;
        return null;
    }
}