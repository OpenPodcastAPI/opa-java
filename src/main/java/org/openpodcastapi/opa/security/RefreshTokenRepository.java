package org.openpodcastapi.opa.security;

import lombok.NonNull;
import org.openpodcastapi.opa.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/// Repository for refresh token interactions
@Repository
public interface RefreshTokenRepository extends JpaRepository<@NonNull RefreshTokenEntity, @NonNull Long> {
    /// Fetches a list of refresh tokens associated with a user
    ///
    /// @param userEntity the [UserEntity] to search for
    /// @return a list of [RefreshTokenEntity] for the user
    List<RefreshTokenEntity> findAllByUser(UserEntity userEntity);

    /// Deletes all tokens that expire before a given date
    ///
    /// @param timestamp the cut-off date
    /// @return a count of deleted items
    int deleteAllByExpiresAtBefore(Instant timestamp);
}
