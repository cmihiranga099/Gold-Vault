package lk.goldvault.backend.repository;

import lk.goldvault.backend.entity.GoldListing;
import lk.goldvault.backend.enums.ListingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoldListingRepository extends JpaRepository<GoldListing, Long> {
    List<GoldListing> findByCustomerId(Long customerId);
    List<GoldListing> findByStatus(ListingStatus status);
}