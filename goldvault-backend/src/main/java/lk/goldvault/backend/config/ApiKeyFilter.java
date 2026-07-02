package lk.goldvault.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.goldvault.backend.entity.PawnShop;
import lk.goldvault.backend.service.ApiKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ApiKeyFilter extends OncePerRequestFilter {

    private final ApiKeyService apiKeyService;

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String POS_API_PREFIX = "/api/v1/pos/";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Only intercept /api/v1/pos/** endpoints
        if (!path.startsWith(POS_API_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String rawKey = request.getHeader(API_KEY_HEADER);
        if (rawKey == null || rawKey.isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("""
                {"success":false,"message":"Missing X-API-Key header"}
                """);
            return;
        }

        Optional<PawnShop> shop = apiKeyService.validateKey(rawKey);
        if (shop.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("""
                {"success":false,"message":"Invalid or revoked API key"}
                """);
            return;
        }

        // Set shop in request attribute so the POS controllers can access it
        request.setAttribute("posShop", shop.get());

        // Set a minimal security context so Spring Security allows the request
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "api_key:" + shop.get().getId(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_SHOP_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }
}