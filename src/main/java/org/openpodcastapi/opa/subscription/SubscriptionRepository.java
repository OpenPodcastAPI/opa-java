package org.openpodcastapi.opa.subscription;

import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/// Repository for subscriptions
@Repository
public interface SubscriptionRepository extends JpaRepository<@NonNull SubscriptionEntity, @NonNull Long> {
    /// Finds an individual subscription by user ID and feed UUID.
    /// Returns `null` if no matching value is found.
    ///
    /// @param userId   the ID of the user
    /// @param feedUuid the UUID of the feed
    /// @return a user subscription, if one matches
    Optional<SubscriptionEntity> findByUserIdAndFeedUuid(Long userId, UUID feedUuid);
}
