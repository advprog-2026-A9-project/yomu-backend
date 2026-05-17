package id.ac.ui.cs.advprog.yomu.auth.bootstrap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.auth.model.User;
import id.ac.ui.cs.advprog.yomu.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class AdminAccountSeeder implements CommandLineRunner {

    private static final String ADMIN_ROLE = "ADMIN";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:Admin123!}")
    private String adminPassword;

    @Value("${app.admin.display-name:Administrator}")
    private String adminDisplayName;

    @Value("${app.admin.email:admin@yomu.local}")
    private String adminEmail;

    @Override
    @Transactional
    public void run(String... args) {
        User existingAdmin = userRepository.findByUsername(adminUsername).orElse(null);
        if (existingAdmin != null) {
            // Ensure seeded admin account keeps admin authority even if role was changed manually.
            if (!ADMIN_ROLE.equalsIgnoreCase(existingAdmin.getRole())) {
                existingAdmin.setRole(ADMIN_ROLE);
                userRepository.save(existingAdmin);
            }
            return;
        }

        User admin = new User();
        admin.setUsername(adminUsername);
        admin.setEmail(adminEmail);
        admin.setDisplayName(adminDisplayName);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole(ADMIN_ROLE);

        userRepository.save(admin);
    }
}