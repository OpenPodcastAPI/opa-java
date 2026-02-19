package org.openpodcastapi.opa.subscription;

import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    /// Returns a paginated list of subscriptions associated with a user.
    ///
    /// @param userId   the ID of the associated user
    /// @param pageable the pagination options
    /// @return a paginated list of subscriptions associated with the user
    Page<@NonNull SubscriptionEntity> findAllByUserId(Long userId, Pageable pageable);

    /// Returns a paginated list of active subscriptions for a user.
    ///
    /// @param userId   the ID of the associated user
    /// @param pageable the pagination options
    /// @return a paginated list of subscriptions associated with the user
    Page<@NonNull SubscriptionEntity> findAllByUserIdAndUnsubscribedAtIsNull(Long userId, Pageable pageable);
}
