package lk.goldvault.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "market_gold_rate")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketGoldRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String purity;

    @Column(name = "rate_per_gram", nullable = false, precision = 10, scale = 2)
    private BigDecimal ratePerGram;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String source = "MANUAL";

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}