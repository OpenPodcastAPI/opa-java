package org.openpodcastapi.opa.user.repository;

import org.openpodcastapi.opa.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> getUserByUuid(UUID uuid);

    Optional<User> getUserByUsername(String username);

    Boolean existsUserByUsername(String username);

    Boolean existsUserByEmail(String email);
}
