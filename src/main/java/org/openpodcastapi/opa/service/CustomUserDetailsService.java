package org.openpodcastapi.opa.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.openpodcastapi.opa.user.UserEntity;
import org.openpodcastapi.opa.user.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/// Custom service for mapping user details
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    /// Returns a mapped custom user details model by username
    ///
    /// @param username the username to map
    /// @throws UsernameNotFoundException if user is not matched by username
    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String username) {
        return userRepository.getUserByUsername(username)
                .map(this::mapToUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException("UserEntity not found"));
    }

    /// Maps a user to a custom user details model
    ///
    /// @param userEntity the [UserEntity] model to map
    private CustomUserDetails mapToUserDetails(UserEntity userEntity) {
        return new CustomUserDetails(
                userEntity.getId(),
                userEntity.getUuid(),
                userEntity.getUsername(),
                userEntity.getPassword(),
                userEntity.getUserRoles()
        );
    }

}

