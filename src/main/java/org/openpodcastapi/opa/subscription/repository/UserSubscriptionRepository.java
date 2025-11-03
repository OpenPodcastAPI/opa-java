package org.openpodcastapi.opa.subscription.repository;

import org.openpodcastapi.opa.subscription.model.UserSubscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {
    Optional<UserSubscription> findByUserIdAndSubscriptionUuid(Long userId, UUID subscriptionUuid);

    Page<UserSubscription> findAllByUserId(Long userId, Pageable pageable);

    Page<UserSubscription> findAllByUserIdAndIsSubscribedTrue(Long userId, Pageable pageable);
}
