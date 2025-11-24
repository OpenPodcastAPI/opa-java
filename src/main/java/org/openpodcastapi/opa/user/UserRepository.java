package org.openpodcastapi.opa.user;

import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<@NonNull UserEntity, @NonNull Long> {
    Optional<UserEntity> getUserByUuid(UUID uuid);

    Optional<UserEntity> getUserByUsername(String username);

    boolean existsUserByEmailOrUsername(String email, String username);

    Optional<UserEntity> findByUsername(String username);
}
