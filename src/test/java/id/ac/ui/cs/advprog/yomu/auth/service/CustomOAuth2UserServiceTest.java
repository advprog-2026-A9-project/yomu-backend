package id.ac.ui.cs.advprog.yomu.auth.service;

import id.ac.ui.cs.advprog.yomu.auth.model.User;
import id.ac.ui.cs.advprog.yomu.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OAuth2UserRequest oAuth2UserRequest;

    @Mock
    private OAuth2User oAuth2User;

    private CustomOAuth2UserService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new CustomOAuth2UserService(userRepository);
    }

    @Test
    void whenUserNotExists_thenSaveNewUser() {
        when(oAuth2User.getAttribute("email")).thenReturn("test@gmail.com");
        when(oAuth2User.getAttribute("name")).thenReturn("Test User");
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.empty());

        service.processOAuth2User(oAuth2User);

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void whenUserAlreadyExists_thenDoNotSave() {
        User existingUser = new User();
        existingUser.setEmail("test@gmail.com");

        when(oAuth2User.getAttribute("email")).thenReturn("test@gmail.com");
        when(oAuth2User.getAttribute("name")).thenReturn("Test User");
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(existingUser));

        service.processOAuth2User(oAuth2User);

        verify(userRepository, never()).save(any(User.class));
    }
}