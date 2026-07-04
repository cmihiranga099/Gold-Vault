package lk.goldvault.backend.dto.response;

import lk.goldvault.backend.enums.ShopStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ShopResponse {
    private Long        id;
    private String      name;
    private String      regNumber;
    private String      ownerName;
    private String      phone;
    private String      email;
    private String      address;
    private BigDecimal  latitude;
    private BigDecimal  longitude;
    private ShopStatus  status;
    private LocalDateTime createdAt;

    // ── License ───────────────────────────────────────────────────────────────
    private String      licenseDocumentUrl;
    private String      licenseStatus;
    private String      licenseRejectReason;
    private LocalDateTime licenseVerifiedAt;
    private String      licenseVerifiedBy;

    // ── Rating (optional, populated when returned with review data) ───────────
    private Double averageRating;
    private Long   totalReviews;
}