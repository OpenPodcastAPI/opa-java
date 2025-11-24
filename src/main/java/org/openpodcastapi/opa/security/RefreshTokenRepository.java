package org.openpodcastapi.opa.security;

import lombok.NonNull;
import org.openpodcastapi.opa.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface RefreshTokenRepository extends JpaRepository<@NonNull RefreshTokenEntity, @NonNull Long> {
    List<RefreshTokenEntity> findAllByUser(UserEntity userEntity);

    int deleteAllByExpiresAtBefore(Instant timestamp);
}
