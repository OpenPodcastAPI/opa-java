package org.openpodcastapi.opa.subscription;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscriptionEntity, Long> {
    Optional<UserSubscriptionEntity> findByUserIdAndSubscriptionUuid(Long userId, UUID subscriptionUuid);

    Page<UserSubscriptionEntity> findAllByUserId(Long userId, Pageable pageable);

    Page<UserSubscriptionEntity> findAllByUserIdAndIsSubscribedTrue(Long userId, Pageable pageable);
}
