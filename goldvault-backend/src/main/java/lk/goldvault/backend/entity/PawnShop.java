package lk.goldvault.backend.entity;

import jakarta.persistence.*;
import lk.goldvault.backend.enums.ShopStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pawn_shop")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PawnShop extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "reg_number", unique = true, length = 50)
    private String regNumber;

    @Column(name = "owner_name", length = 100)
    private String ownerName;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String address;

    // ── License verification fields ──────────────────────────────────────────
    @Column(name = "license_document_url", length = 500)
    private String licenseDocumentUrl;

    @Column(name = "license_status", nullable = false, length = 20)
    @Builder.Default
    private String licenseStatus = "PENDING"; // PENDING, VERIFIED, REJECTED

    @Column(name = "license_reject_reason", columnDefinition = "TEXT")
    private String licenseRejectReason;

    @Column(name = "license_verified_at")
    private LocalDateTime licenseVerifiedAt;

    @Column(name = "license_verified_by", length = 100)
    private String licenseVerifiedBy;

    // ── Location ─────────────────────────────────────────────────────────────
    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ShopStatus status = ShopStatus.PENDING;
}