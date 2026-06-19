package lk.goldvault.backend.entity;

import jakarta.persistence.*;
import lk.goldvault.backend.enums.ShopStatus;
import lombok.*;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ShopStatus status = ShopStatus.PENDING;
}