package org.openpodcastapi.opa.subscription;

import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<@NonNull UserSubscriptionEntity, @NonNull Long> {
    Optional<UserSubscriptionEntity> findByUserIdAndSubscriptionUuid(Long userId, UUID subscriptionUuid);

    Page<@NonNull UserSubscriptionEntity> findAllByUserId(Long userId, Pageable pageable);

    Page<@NonNull UserSubscriptionEntity> findAllByUserIdAndUnsubscribedAtNotEmpty(Long userId, Pageable pageable);
}
