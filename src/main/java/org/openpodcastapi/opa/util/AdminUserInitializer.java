package org.openpodcastapi.opa.util;

import org.jspecify.annotations.NonNull;
import org.openpodcastapi.opa.user.UserEntity;
import org.openpodcastapi.opa.user.UserRepository;
import org.openpodcastapi.opa.user.UserRoles;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

/// Creates a default admin user for the system
@Component
public class AdminUserInitializer implements ApplicationRunner {
    private static final Logger log = getLogger(AdminUserInitializer.class);
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;
    @Value("${admin.username}")
    private String username;
    @Value("${admin.password}")
    private String password;
    @Value("${admin.email}")
    private String email;

    /// Required-args constructor
    ///
    /// @param userRepository the user repository used for user interactions
    /// @param encoder        the password encoder used to encrypt the admin password
    public AdminUserInitializer(UserRepository userRepository, BCryptPasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

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
