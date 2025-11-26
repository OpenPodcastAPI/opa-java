package org.openpodcastapi.opa.subscription;

import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/// Repository for subscription interactions
@Repository
public interface SubscriptionRepository extends JpaRepository<@NonNull SubscriptionEntity, @NonNull Long> {
    /// Finds a single subscription by UUID. Returns `null` if no value exists.
    ///
    /// @param uuid the [UUID] to match
    /// @return the matching [SubscriptionEntity]
    Optional<SubscriptionEntity> findByUuid(UUID uuid);
}
