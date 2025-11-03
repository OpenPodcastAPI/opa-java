package org.openpodcastapi.opa.service;

import lombok.RequiredArgsConstructor;
import org.openpodcastapi.opa.user.model.User;
import org.openpodcastapi.opa.user.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
    public UserDetails loadUserByUsername(String username) {
        return userRepository.getUserByUsername(username)
                .map(this::mapToUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /// Maps a user to a custom user details model
    ///
    /// @param user the user model to map
    private CustomUserDetails mapToUserDetails(User user) {
        return new CustomUserDetails(
                user.getId(),
                user.getUuid(),
                user.getUsername(),
                user.getPassword(),
                user.getUserRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                        .toList()
        );
    }

}

