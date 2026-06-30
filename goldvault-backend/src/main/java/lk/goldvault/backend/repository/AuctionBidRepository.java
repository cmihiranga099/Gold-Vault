package lk.goldvault.backend.repository;

import lk.goldvault.backend.entity.AuctionBid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuctionBidRepository extends JpaRepository<AuctionBid, Long> {
    List<AuctionBid> findByAuctionIdOrderByAmountDesc(Long auctionId);
}