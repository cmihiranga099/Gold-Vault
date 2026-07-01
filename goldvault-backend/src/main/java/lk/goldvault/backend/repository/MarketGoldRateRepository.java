package lk.goldvault.backend.repository;

import lk.goldvault.backend.entity.MarketGoldRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MarketGoldRateRepository extends JpaRepository<MarketGoldRate, Long> {

    /** Today's market rate for a purity */
    Optional<MarketGoldRate> findFirstByPurityAndEffectiveDateOrderByCreatedAtDesc(
            String purity, LocalDate date);

    /** Latest rate for each purity (today or most recent) */
    @Query(value = """
        SELECT m.* FROM market_gold_rate m
        INNER JOIN (
            SELECT purity, MAX(effective_date) AS max_date
            FROM market_gold_rate
            GROUP BY purity
        ) latest ON m.purity = latest.purity AND m.effective_date = latest.max_date
        ORDER BY m.purity
        """, nativeQuery = true)
    List<MarketGoldRate> findLatestForAllPurities();

    /** Last 30 days of rates for a purity — for the history chart */
    List<MarketGoldRate> findByPurityAndEffectiveDateBetweenOrderByEffectiveDateAsc(
            String purity, LocalDate from, LocalDate to);
}