package lk.goldvault.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "shop_api_key")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private PawnShop shop;

    /** SHA-256 hash of the raw key — never store plaintext */
    @Column(name = "api_key", nullable = false, unique = true, length = 64)
    private String apiKey;

    /** First 8 chars of the raw key — shown in admin UI for identification */
    @Column(name = "key_prefix", nullable = false, length = 8)
    private String keyPrefix;

    @Column(length = 100)
    private String label;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Column(name = "last_used")
    private LocalDateTime lastUsed;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}