package org.openpodcastapi.opa.subscription;

import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<@NonNull SubscriptionEntity, @NonNull Long> {
    Optional<SubscriptionEntity> findByUuid(UUID uuid);
}
