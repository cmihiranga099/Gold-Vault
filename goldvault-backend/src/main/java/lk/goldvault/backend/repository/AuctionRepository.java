package lk.goldvault.backend.repository;

import lk.goldvault.backend.entity.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {
    Optional<Auction> findByTicketId(Long ticketId);
    List<Auction> findByStatusOrderByEndsAtAsc(String status);
    List<Auction> findByShopIdOrderByCreatedAtDesc(Long shopId);
    List<Auction> findByStatusAndEndsAtBefore(String status, LocalDateTime time);
    boolean existsByTicketId(Long ticketId);
}