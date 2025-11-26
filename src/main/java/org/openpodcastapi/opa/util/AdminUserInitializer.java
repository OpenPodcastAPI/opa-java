package org.openpodcastapi.opa.util;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.openpodcastapi.opa.user.UserEntity;
import org.openpodcastapi.opa.user.UserRepository;
import org.openpodcastapi.opa.user.UserRoles;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/// Creates a default admin user for the system
@Component
@RequiredArgsConstructor
@Log4j2
public class AdminUserInitializer implements ApplicationRunner {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;
    @Value("${admin.username}")
    private String username;
    @Value("${admin.password}")
    private String password;
    @Value("${admin.email}")
    private String email;

    /// Creates a default admin user for the system
    ///
    /// @param args the application arguments
    @Override
    public void run(@NonNull ApplicationArguments args) {
        if (userRepository.findUserByUsername(username).isEmpty()) {
            final var adminUserEntity = new UserEntity();
            adminUserEntity.setUsername(username);
            adminUserEntity.setEmail(email);
            adminUserEntity.setPassword(encoder.encode(password));
            adminUserEntity.setUserRoles(Set.of(UserRoles.ADMIN, UserRoles.USER));
            userRepository.save(adminUserEntity);

            log.info("✅ Admin user created: {} / {}", username, password);
        }
    }
}
