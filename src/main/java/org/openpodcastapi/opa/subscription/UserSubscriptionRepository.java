package org.openpodcastapi.opa.subscription;

import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/// Repository for user subscription interactions
@Repository
public interface UserSubscriptionRepository extends JpaRepository<@NonNull UserSubscriptionEntity, @NonNull Long> {
    /// Finds an individual [UserSubscriptionEntity] by user ID and feed UUID.
    /// Returns `null` if no matching value is found.
    ///
    /// @param userId           the ID of the user
    /// @param subscriptionUuid the UUID of the subscription
    /// @return a [UserSubscriptionEntity], if one matches
    Optional<UserSubscriptionEntity> findByUserIdAndSubscriptionUuid(Long userId, UUID subscriptionUuid);

    /// Returns a paginated list of [UserSubscriptionEntity] objects associated with a user.
    ///
    /// @param userId   the ID of the associated user
    /// @param pageable the [Pageable] object containing pagination information
    /// @return a [Page] of [UserSubscriptionEntity] associated with the user
    Page<@NonNull UserSubscriptionEntity> findAllByUserId(Long userId, Pageable pageable);

    /// Returns a paginated list of [UserSubscriptionEntity] for a user where the [UserSubscriptionEntity#unsubscribedAt]
    /// field is not empty.
    ///
    /// @param userId   the ID of the associated user
    /// @param pageable the [Pageable] object containing pagination information
    /// @return a [Page] of [UserSubscriptionEntity] associated with the user
    Page<@NonNull UserSubscriptionEntity> findAllByUserIdAndUnsubscribedAtNotEmpty(Long userId, Pageable pageable);
}
