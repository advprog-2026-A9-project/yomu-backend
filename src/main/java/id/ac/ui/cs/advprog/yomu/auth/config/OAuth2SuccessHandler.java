package id.ac.ui.cs.advprog.yomu.auth.config;

import id.ac.ui.cs.advprog.yomu.auth.event.UserCreatedEvent;
import id.ac.ui.cs.advprog.yomu.auth.model.User;
import id.ac.ui.cs.advprog.yomu.auth.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import id.ac.ui.cs.advprog.yomu.auth.event.UserCreatedEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Value("${frontend.url:http://localhost:5173}")
    private String frontendUrl;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        

        User user = userRepository.findByEmail(email)
                .or(() -> userRepository.findByEmailIgnoreCase(email))
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setDisplayName(name);
                    newUser.setRole("PELAJAR");
                    newUser.setPassword("");
                    String baseUsername = email.split("@")[0].replaceAll("[^a-zA-Z0-9_]", "_");
                    newUser.setUsername(baseUsername);
                    User saved = userRepository.save(newUser);
                    eventPublisher.publishEvent(new UserCreatedEvent(this, saved.getId(), saved.getUsername(), saved.getDisplayName()));
                    return saved;
                });

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        response.sendRedirect(frontendUrl + "/oauth2/callback?token=" + token);
    }
}