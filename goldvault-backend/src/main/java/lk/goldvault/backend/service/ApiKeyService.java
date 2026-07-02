package lk.goldvault.backend.service;

import lk.goldvault.backend.entity.PawnShop;
import lk.goldvault.backend.entity.ShopApiKey;
import lk.goldvault.backend.repository.PawnShopRepository;
import lk.goldvault.backend.repository.ShopApiKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiKeyService {

    private final ShopApiKeyRepository shopApiKeyRepository;
    private final PawnShopRepository   pawnShopRepository;

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generates a new API key for a shop.
     * Returns the PLAINTEXT key once — it is never retrievable again after this call.
     * Stores the SHA-256 hash in DB.
     */
    @Transactional
    public GeneratedKey generateKey(Long shopId, String label) {
        PawnShop shop = pawnShopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found: " + shopId));

        // Generate 32 random bytes → Base64URL string
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String rawKey = "gv_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        String hashed = sha256(rawKey);
        String prefix = rawKey.substring(0, 8);

        ShopApiKey entity = ShopApiKey.builder()
                .shop(shop)
                .apiKey(hashed)
                .keyPrefix(prefix)
                .label(label)
                .enabled(true)
                .build();

        shopApiKeyRepository.save(entity);

        log.info("[API Key] Generated key for shop {} (prefix: {})", shopId, prefix);

        return new GeneratedKey(rawKey, prefix, label);
    }

    /** Validates a raw API key from the request header. Returns the shop if valid. */
    public Optional<PawnShop> validateKey(String rawKey) {
        if (rawKey == null || !rawKey.startsWith("gv_")) return Optional.empty();

        String hashed = sha256(rawKey);
        Optional<ShopApiKey> keyEntity = shopApiKeyRepository.findByApiKeyAndEnabledTrue(hashed);

        if (keyEntity.isPresent()) {
            ShopApiKey key = keyEntity.get();
            key.setLastUsed(LocalDateTime.now());
            shopApiKeyRepository.save(key);
            return Optional.of(key.getShop());
        }

        return Optional.empty();
    }

    /** List all API keys for a shop (no raw keys exposed — only prefix + label). */
    public List<ApiKeyInfo> listKeys(Long shopId) {
        return shopApiKeyRepository.findByShopId(shopId)
                .stream()
                .map(k -> new ApiKeyInfo(
                        k.getId(), k.getKeyPrefix() + "...", k.getLabel(),
                        k.isEnabled(), k.getLastUsed(), k.getCreatedAt()))
                .toList();
    }

    /** Revoke a key. */
    @Transactional
    public void revokeKey(Long keyId, Long shopId) {
        ShopApiKey key = shopApiKeyRepository.findById(keyId)
                .orElseThrow(() -> new RuntimeException("API key not found: " + keyId));
        if (!key.getShop().getId().equals(shopId)) {
            throw new RuntimeException("Key does not belong to this shop.");
        }
        key.setEnabled(false);
        shopApiKeyRepository.save(key);
        log.info("[API Key] Revoked key {} for shop {}", keyId, shopId);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    // ── Inner records ─────────────────────────────────────────────────────────────

    public record GeneratedKey(String rawKey, String prefix, String label) {}

    public record ApiKeyInfo(
            Long id, String keyPreview, String label,
            boolean enabled, LocalDateTime lastUsed, LocalDateTime createdAt) {}
}