package lk.goldvault.backend.entity;

import jakarta.persistence.*;
import lk.goldvault.backend.enums.GoldPurity;
import lk.goldvault.backend.enums.ListingStatus;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "gold_listing")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoldListing extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(length = 200)
    private String description;

    @Column(name = "weight_grams", nullable = false, precision = 8, scale = 3)
    private BigDecimal weightGrams;

    @Column(nullable = false)
    private GoldPurity purity;

    @Column(name = "asking_price", precision = 12, scale = 2)
    private BigDecimal askingPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ListingStatus status = ListingStatus.OPEN;

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GoldOffer> offers = new ArrayList<>();
}