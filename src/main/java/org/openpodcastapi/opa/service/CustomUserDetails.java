package org.openpodcastapi.opa.service;

import org.jspecify.annotations.NonNull;
import org.openpodcastapi.opa.user.UserRoles;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/// Implements a custom user details service to expose UUID information
///
/// @param id       the user ID
/// @param uuid     the user UUID
/// @param username the user's username
/// @param password the user's hashed password
/// @param roles    the user's assigned roles
public record CustomUserDetails(Long id, UUID uuid, String username, String password,
                                Set<UserRoles> roles) implements UserDetails {

    @Override
    public @NonNull String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toSet());
    }
}
