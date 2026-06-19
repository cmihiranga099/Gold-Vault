package lk.goldvault.backend.repository;

import lk.goldvault.backend.entity.GoldOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoldOfferRepository extends JpaRepository<GoldOffer, Long> {
    List<GoldOffer> findByListingId(Long listingId);
    List<GoldOffer> findByShopId(Long shopId);
}