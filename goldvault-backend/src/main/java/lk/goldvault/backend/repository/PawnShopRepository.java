package lk.goldvault.backend.repository;

import lk.goldvault.backend.entity.PawnShop;
import lk.goldvault.backend.enums.ShopStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PawnShopRepository extends JpaRepository<PawnShop, Long> {
    Optional<PawnShop> findByRegNumber(String regNumber);
    List<PawnShop> findByStatus(ShopStatus status);
    boolean existsByRegNumber(String regNumber);
}