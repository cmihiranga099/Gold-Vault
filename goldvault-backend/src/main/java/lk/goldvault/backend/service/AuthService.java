package lk.goldvault.backend.service;

import lk.goldvault.backend.dto.request.LoginRequest;
import lk.goldvault.backend.dto.request.RegisterRequest;
import lk.goldvault.backend.dto.response.AuthResponse;
import lk.goldvault.backend.entity.AppUser;
import lk.goldvault.backend.entity.Customer;
import lk.goldvault.backend.entity.PawnShop;
import lk.goldvault.backend.enums.UserRole;
import lk.goldvault.backend.repository.AppUserRepository;
import lk.goldvault.backend.repository.CustomerRepository;
import lk.goldvault.backend.repository.PawnShopRepository;
import lk.goldvault.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PawnShopRepository pawnShopRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        AppUser user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .shopId(user.getShopId())
                .customerId(user.getCustomerId())
                .build();
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered: " + request.getEmail());
        }
        if (customerRepository.existsByNic(request.getNic())) {
            throw new RuntimeException("A customer with this NIC already exists: " + request.getNic());
        }

        PawnShop shop = pawnShopRepository.findById(request.getShopId())
                .orElseThrow(() -> new RuntimeException("Shop not found with id: " + request.getShopId()));

        Customer customer = Customer.builder()
                .shop(shop)
                .fullName(request.getFullName())
                .nic(request.getNic())
                .phone(request.getPhone())
                .email(request.getEmail())
                .build();
        customer = customerRepository.save(customer);

        AppUser user = AppUser.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .role(UserRole.ROLE_CUSTOMER)
                .customerId(customer.getId())
                .enabled(true)
                .build();
        user = userRepository.save(user);

        String token = jwtUtil.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .shopId(null)
                .customerId(customer.getId())
                .build();
    }
}