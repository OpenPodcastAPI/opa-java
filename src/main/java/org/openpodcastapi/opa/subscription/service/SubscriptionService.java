package org.openpodcastapi.opa.subscription.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.openpodcastapi.opa.subscription.dto.SubscriptionCreateDto;
import org.openpodcastapi.opa.subscription.mapper.SubscriptionMapper;
import org.openpodcastapi.opa.subscription.model.Subscription;
import org.openpodcastapi.opa.subscription.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;

    /// Fetches an existing repository from the database or creates a new one if none is found
    ///
    /// @param dto the [SubscriptionCreateDto] containing the subscription data
    /// @return the fetched or created [Subscription]
    @Transactional
    protected Subscription fetchOrCreateSubscription(SubscriptionCreateDto dto) {
        UUID feedUuid = UUID.fromString(dto.uuid());
        return subscriptionRepository
                .findByUuid(feedUuid)
                .orElseGet(() -> {
                    log.debug("Creating new subscription with UUID {}", dto.uuid());
                    return subscriptionRepository.save(subscriptionMapper.toEntity(dto));
                });
    }
}
