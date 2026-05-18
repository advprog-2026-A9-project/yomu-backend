package id.ac.ui.cs.advprog.yomu.auth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import id.ac.ui.cs.advprog.yomu.auth.filter.JwtAuthenticationFilter;
import id.ac.ui.cs.advprog.yomu.auth.service.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String ROLE_ADMIN = "ADMIN";

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()

                        // Public clan read access for dashboard/discover pages
                        .requestMatchers(HttpMethod.GET, "/api/clans", "/api/clans/*", "/api/clans/leaderboard", "/api/clans/leaderboard/**").permitAll()
                        .requestMatchers("/api/clans/me").authenticated()

                        // Gamification Admin Access
                        .requestMatchers("/api/gamification/admin/**").hasRole(ROLE_ADMIN)
                        .requestMatchers("/api/gamification/progress/**").authenticated()
                        .requestMatchers("/api/gamification/showcase/**").authenticated()

                        // Role-based access untuk reading
                        .requestMatchers(HttpMethod.POST, "/api/reading-texts/*/quiz").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/reading-texts/**").hasRole(ROLE_ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/api/reading-texts/**").hasRole(ROLE_ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/reading-texts/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/seasons/end").hasRole(ROLE_ADMIN)
                        // Protected endpoints (harus autentikasi)
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        .requestMatchers("/api/readings/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/clans").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/clans/*/edit").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/clans/*/delete").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/clans/*/kick/**").authenticated()
                        .requestMatchers("/api/discussion/**").authenticated()
                        .anyRequest().authenticated())
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives(
                                        "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self'; connect-src 'self'"))
                        .xssProtection(Customizer.withDefaults()))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"message\": \"Forbidden: Insufficient permissions\"}");
                        }))
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(info -> info
                                .userService(customOAuth2UserService))
                        .defaultSuccessUrl("/api/auth/me"));

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}