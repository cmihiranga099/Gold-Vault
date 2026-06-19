package lk.goldvault.backend.repository;

import lk.goldvault.backend.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByNic(String nic);
    boolean existsByNic(String nic);
    List<Customer> findByShopId(Long shopId);
    List<Customer> findByShopIdAndFullNameContainingIgnoreCase(Long shopId, String name);
}