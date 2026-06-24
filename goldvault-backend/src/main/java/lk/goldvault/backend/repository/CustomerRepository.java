package lk.goldvault.backend.repository;

import lk.goldvault.backend.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByNic(String nic);
    boolean existsByNic(String nic);
    List<Customer> findByShopId(Long shopId);
    List<Customer> findByShopIdAndFullNameContainingIgnoreCase(Long shopId, String name);

    Page<Customer> findByShopId(Long shopId, Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE c.shop.id = :shopId " +
           "AND (LOWER(c.fullName) LIKE LOWER(CONCAT('%', :term, '%')) " +
           "     OR LOWER(c.nic) LIKE LOWER(CONCAT('%', :term, '%')))")
    Page<Customer> search(@Param("shopId") Long shopId, @Param("term") String term, Pageable pageable);
}