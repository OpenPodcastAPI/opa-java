package org.openpodcastapi.opa.feed;

import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/// Repository for subscription feed interactions
@Repository
public interface FeedRepository extends JpaRepository<@NonNull FeedEntity, @NonNull Long> {
    /// Finds a single subscription by UUID.
    ///
    /// @param uuid the UUIDv5 value to match
    /// @return an optional [FeedEntity] match
    Optional<FeedEntity> findByUuid(UUID uuid);
}
