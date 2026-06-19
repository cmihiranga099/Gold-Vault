package lk.goldvault.backend.repository;

import lk.goldvault.backend.entity.GoldItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoldItemRepository extends JpaRepository<GoldItem, Long> {
    List<GoldItem> findByTicketId(Long ticketId);
}