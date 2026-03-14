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

@Component
@RequiredArgsConstructor
@ConditionalOnBean(JwtUtil.class)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {

            String token = extractTokenFromRequest(request);

            if (token != null && jwtUtil.validateToken(token)) {
              
                String userId = jwtUtil.extractUserId(token);
                String username = jwtUtil.extractUsername(token);
                String role = jwtUtil.extractRole(token);

                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, Collections.singletonList(authority));

                
                authentication.setDetails(new JwtAuthenticationDetails(userId, username, role));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
        
            logger.error("Tidak bisa set user authentication", e);
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}

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
