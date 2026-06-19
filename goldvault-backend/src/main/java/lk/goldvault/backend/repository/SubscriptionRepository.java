package lk.goldvault.backend.repository;

import lk.goldvault.backend.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByShopId(Long shopId);
    Optional<Subscription> findFirstByShopIdOrderByEndDateDesc(Long shopId);
}