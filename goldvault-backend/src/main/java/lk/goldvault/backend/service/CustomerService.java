package lk.goldvault.backend.service;

import lk.goldvault.backend.dto.request.CustomerRequest;
import lk.goldvault.backend.dto.response.CustomerResponse;
import lk.goldvault.backend.entity.Customer;
import lk.goldvault.backend.entity.PawnShop;
import lk.goldvault.backend.repository.CustomerRepository;
import lk.goldvault.backend.repository.PawnShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PawnShopRepository pawnShopRepository;

    public CustomerResponse register(Long shopId, CustomerRequest request) {
        if (customerRepository.existsByNic(request.getNic())) {
            throw new RuntimeException("A customer with this NIC already exists: " + request.getNic());
        }

        PawnShop shop = pawnShopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found with id: " + shopId));

        Customer customer = Customer.builder()
                .shop(shop)
                .fullName(request.getFullName())
                .nic(request.getNic())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(request.getAddress())
                .dob(request.getDob())
                .nicPhotoUrl(request.getNicPhotoUrl())
                .build();

        customer = customerRepository.save(customer);
        return toResponse(customer);
    }

    public CustomerResponse getById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
        return toResponse(customer);
    }

    public CustomerResponse getByNic(String nic) {
        Customer customer = customerRepository.findByNic(nic)
                .orElseThrow(() -> new RuntimeException("Customer not found with NIC: " + nic));
        return toResponse(customer);
    }

    public List<CustomerResponse> getByShop(Long shopId) {
        return customerRepository.findByShopId(shopId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<CustomerResponse> searchByName(Long shopId, String name) {
        return customerRepository.findByShopIdAndFullNameContainingIgnoreCase(shopId, name)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));

        customer.setFullName(request.getFullName());
        customer.setPhone(request.getPhone());
        customer.setEmail(request.getEmail());
        customer.setAddress(request.getAddress());
        customer.setDob(request.getDob());
        if (request.getNicPhotoUrl() != null) {
            customer.setNicPhotoUrl(request.getNicPhotoUrl());
        }

        customer = customerRepository.save(customer);
        return toResponse(customer);
    }

    private CustomerResponse toResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .fullName(customer.getFullName())
                .nic(customer.getNic())
                .phone(customer.getPhone())
                .email(customer.getEmail())
                .address(customer.getAddress())
                .dob(customer.getDob())
                .kycStatus(customer.getKycStatus())
                .nicPhotoUrl(customer.getNicPhotoUrl())
                .shopId(customer.getShop().getId())
                .createdAt(customer.getCreatedAt())
                .build();
    }
}