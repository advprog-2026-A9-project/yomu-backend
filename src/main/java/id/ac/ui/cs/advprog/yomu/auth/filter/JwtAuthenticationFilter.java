package id.ac.ui.cs.advprog.yomu.auth.filter;

import id.ac.ui.cs.advprog.yomu.auth.config.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT Authentication Filter
 * Mengecek setiap HTTP request untuk JWT token di header Authorization.
 * Jika token valid, masukkan user info ke SecurityContextHolder.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnBean(JwtUtil.class)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // Ekstrak token dari header Authorization: Bearer <token>
            String token = extractTokenFromRequest(request);

            if (token != null && jwtUtil.validateToken(token)) {
                // Token valid, ekstrak data user
                String userId = jwtUtil.extractUserId(token);
                String username = jwtUtil.extractUsername(token);
                String role = jwtUtil.extractRole(token);

                // Buat authentication token dan masukkan ke SecurityContextHolder
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, Collections.singletonList(authority));

                // Simpan tambahan info (userId dan role) di authentication details
                authentication.setDetails(new JwtAuthenticationDetails(userId, username, role));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            // Jika terjadi error saat parsing token, lanjutkan ke filter berikutnya
            logger.error("Tidak bisa set user authentication", e);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Ekstrak JWT token dari Authorization header
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}

/**
 * Custom details untuk menyimpan info tambahan dari JWT
 */
class JwtAuthenticationDetails {
    public final String userId;
    public final String username;
    public final String role;

    JwtAuthenticationDetails(String userId, String username, String role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
    }
}
