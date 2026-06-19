package lk.goldvault.backend.repository;

import lk.goldvault.backend.entity.GoldRate;
import lk.goldvault.backend.enums.GoldPurity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoldRateRepository extends JpaRepository<GoldRate, Long> {
    List<GoldRate> findByIsActiveTrue();
    List<GoldRate> findByShopIdAndIsActiveTrue(Long shopId);
    Optional<GoldRate> findFirstByShopIdAndPurityAndIsActiveTrueOrderByEffectiveDateDesc(
            Long shopId, GoldPurity purity);
}