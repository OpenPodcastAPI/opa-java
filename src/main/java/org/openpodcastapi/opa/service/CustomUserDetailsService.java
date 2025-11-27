package org.openpodcastapi.opa.service;

import org.jspecify.annotations.NonNull;
import org.openpodcastapi.opa.user.UserEntity;
import org.openpodcastapi.opa.user.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

/// Custom service for mapping user details
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    /// Required-args constructor
    ///
    /// @param userRepository the user repository for user interactions
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /// Returns a mapped custom user details model by username
    ///
    /// @param username the username to map
    /// @throws UsernameNotFoundException if user is not matched by username
    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String username) {
        return userRepository.findUserByUsername(username)
                .map(this::mapToUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException("UserEntity not found"));
    }

    /// Maps a user to a custom user details model
    ///
    /// @param userEntity the user model to map
    private CustomUserDetails mapToUserDetails(UserEntity userEntity) {
        return new CustomUserDetails(
                userEntity.getId(),
                userEntity.getUuid(),
                userEntity.getUsername(),
                userEntity.getPassword(),
                userEntity.getUserRoles() == null
                        ? Set.of()
                        : userEntity.getUserRoles().stream()
                        .collect(Collectors.toUnmodifiableSet())
        );
    }

}

