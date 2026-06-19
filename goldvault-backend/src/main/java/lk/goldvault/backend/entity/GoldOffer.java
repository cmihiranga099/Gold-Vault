package lk.goldvault.backend.entity;

import jakarta.persistence.*;
import lk.goldvault.backend.enums.OfferStatus;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "gold_offer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoldOffer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private GoldListing listing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private PawnShop shop;

    @Column(name = "offer_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal offerPrice;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OfferStatus status = OfferStatus.PENDING;
}