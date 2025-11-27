package org.openpodcastapi.opa.user;

import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/// A repository for user interactions
@Repository
public interface UserRepository extends JpaRepository<@NonNull UserEntity, @NonNull Long> {
    /// Finds a single user by UUID. Returns `null` if no entity is found.
    ///
    /// @param uuid the UUID of the user
    /// @return the found user
    Optional<UserEntity> findUserByUuid(UUID uuid);

    /// Finds a single user by username. Returns `null` if no entity is found.
    ///
    /// @param username the username of the user
    /// @return the found user
    Optional<UserEntity> findUserByUsername(String username);

    /// Performs a check to see if there is an existing entity with the same username or email address
    ///
    /// @param email    the email address to check
    /// @param username the username to check
    /// @return a boolean value representing whether an existing user was found
    boolean existsUserByEmailOrUsername(String email, String username);
}
