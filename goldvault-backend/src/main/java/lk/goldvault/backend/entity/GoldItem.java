package lk.goldvault.backend.entity;

import jakarta.persistence.*;
import lk.goldvault.backend.enums.GoldPurity;
import lk.goldvault.backend.enums.GoldType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "gold_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoldItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private PawnTicket ticket;

    @Column(nullable = false, length = 200)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "gold_type", nullable = false)
    @Builder.Default
    private GoldType goldType = GoldType.OTHER;

    @Column(name = "weight_grams", nullable = false, precision = 8, scale = 3)
    private BigDecimal weightGrams;

    @Column(nullable = false)
    private GoldPurity purity;

    @Column(name = "estimated_value", precision = 12, scale = 2)
    private BigDecimal estimatedValue;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}