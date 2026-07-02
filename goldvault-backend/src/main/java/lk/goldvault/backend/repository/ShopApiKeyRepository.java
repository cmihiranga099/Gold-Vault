package lk.goldvault.backend.repository;

import lk.goldvault.backend.entity.ShopApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopApiKeyRepository extends JpaRepository<ShopApiKey, Long> {

    Optional<ShopApiKey> findByApiKeyAndEnabledTrue(String hashedKey);
    List<ShopApiKey> findByShopId(Long shopId);
}