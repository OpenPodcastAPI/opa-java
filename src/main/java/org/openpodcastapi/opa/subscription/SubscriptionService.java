package org.openpodcastapi.opa.subscription;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.openpodcastapi.opa.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final UserSubscriptionMapper userSubscriptionMapper;
    private final UserRepository userRepository;

    /// Fetches an existing repository from the database or creates a new one if none is found
    ///
    /// @param dto the [SubscriptionDTO.SubscriptionCreateDTO] containing the subscription data
    /// @return the fetched or created [SubscriptionEntity]
    protected SubscriptionEntity fetchOrCreateSubscription(SubscriptionDTO.SubscriptionCreateDTO dto) {
        final var feedUuid = UUID.fromString(dto.uuid());
        return subscriptionRepository
                .findByUuid(feedUuid)
                .orElseGet(() -> {
                    log.debug("Creating new subscription with UUID {}", dto.uuid());
                    return subscriptionRepository.save(subscriptionMapper.toEntity(dto));
                });
    }

    /// Fetches a single subscription for an authenticated userEntity, if it exists
    ///
    /// @param subscriptionUuid the UUID of the subscription
    /// @param userId           the database ID of the user
    /// @return a [SubscriptionDTO.UserSubscriptionDTO] of the user subscription
    /// @throws EntityNotFoundException if no entry is found
    @Transactional(readOnly = true)
    public SubscriptionDTO.UserSubscriptionDTO getUserSubscriptionBySubscriptionUuid(UUID subscriptionUuid, Long userId) {
        log.debug("Fetching subscription {} for userEntity {}", subscriptionUuid, userId);
        final var userSubscription = userSubscriptionRepository.findByUserIdAndSubscriptionUuid(userId, subscriptionUuid)
                .orElseThrow(() -> new EntityNotFoundException("subscription not found for userEntity"));

        log.debug("Subscription {} for userEntity {} found", subscriptionUuid, userId);
        return userSubscriptionMapper.toDto(userSubscription);
    }

    /// Gets all subscriptions for the authenticated userEntity
    ///
    /// @param userId the database ID of the authenticated userEntity
    /// @return a paginated set of [SubscriptionDTO.UserSubscriptionDTO] objects
    @Transactional(readOnly = true)
    public Page<SubscriptionDTO.UserSubscriptionDTO> getAllSubscriptionsForUser(Long userId, Pageable pageable) {
        log.debug("Fetching subscriptions for {}", userId);
        return userSubscriptionRepository
                .findAllByUserId(userId, pageable)
                .map(userSubscriptionMapper::toDto);
    }

    /// Gets all active subscriptions for the authenticated user
    ///
    /// @param userId the database ID of the authenticated user
    /// @return a paginated set of [SubscriptionDTO.UserSubscriptionDTO] objects
    @Transactional(readOnly = true)
    public Page<SubscriptionDTO.UserSubscriptionDTO> getAllActiveSubscriptionsForUser(Long userId, Pageable pageable) {
        log.debug("Fetching all active subscriptions for {}", userId);
        return userSubscriptionRepository.findAllByUserIdAndIsSubscribedTrue(userId, pageable).map(userSubscriptionMapper::toDto);
    }

    /// Persists a new user subscription to the database
    /// If an existing entry is found for the user and subscription, the `isSubscribed` property is set to `true`
    ///
    /// @param subscriptionEntity the target [SubscriptionEntity]
    /// @param userId             the ID of the target user
    /// @return a [SubscriptionDTO.UserSubscriptionDTO] representation of the subscription link
    /// @throws EntityNotFoundException if no matching user is found
    protected SubscriptionDTO.UserSubscriptionDTO persistUserSubscription(SubscriptionEntity subscriptionEntity, Long userId) {
        final var userEntity = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("user not found"));

        log.debug("{}", userEntity);

        final var newSubscription = userSubscriptionRepository.findByUserIdAndSubscriptionUuid(userId, subscriptionEntity.getUuid()).orElseGet(() -> {
            log.debug("Creating new subscription for user {} and subscription {}", userId, subscriptionEntity.getUuid());
            final var createdSubscriptionEntity = new UserSubscriptionEntity();
            createdSubscriptionEntity.setIsSubscribed(true);
            createdSubscriptionEntity.setUser(userEntity);
            createdSubscriptionEntity.setSubscription(subscriptionEntity);
            return userSubscriptionRepository.save(createdSubscriptionEntity);
        });

        newSubscription.setIsSubscribed(true);
        return userSubscriptionMapper.toDto(userSubscriptionRepository.save(newSubscription));
    }

    /// Creates [UserSubscriptionEntity] links in bulk. If the [SubscriptionEntity] isn't already in the system, this is added before the user is subscribed.
    ///
    /// @param requests a list of [SubscriptionDTO.SubscriptionCreateDTO] objects to create
    /// @param userId   the ID of the requesting user
    /// @return a [SubscriptionDTO.BulkSubscriptionResponseDTO] DTO containing a list of successes and failures
    @Transactional
    public SubscriptionDTO.BulkSubscriptionResponseDTO addSubscriptions(List<SubscriptionDTO.SubscriptionCreateDTO> requests, Long userId) {
        List<SubscriptionDTO.UserSubscriptionDTO> successes = new ArrayList<>();
        List<SubscriptionDTO.SubscriptionFailureDTO> failures = new ArrayList<>();

        log.info("{}", requests);

        for (var subscriptionObject : requests) {
            try {
                // Fetch or create the subscription object to subscribe the user to
                final var subscriptionEntity = this.fetchOrCreateSubscription(subscriptionObject);
                log.debug("{}", subscriptionEntity);
                // If all is successful, persist the new UserSubscriptionEntity and add a UserSubscriptionDTO to the successes list
                successes.add(persistUserSubscription(subscriptionEntity, userId));
            } catch (IllegalArgumentException _) {
                // If the UUID of the feed is invalid, add a new failure to the failures list
                failures.add(new SubscriptionDTO.SubscriptionFailureDTO(subscriptionObject.uuid(), subscriptionObject.feedUrl(), "invalid UUID format"));
            } catch (Exception e) {
                // If another failure is encountered, add it to the failures list
                failures.add(new SubscriptionDTO.SubscriptionFailureDTO(subscriptionObject.uuid(), subscriptionObject.feedUrl(), e.getMessage()));
            }
        }

        // Return the entire DTO of successes and failures
        return new SubscriptionDTO.BulkSubscriptionResponseDTO(successes, failures);
    }

    /// Updates the status of a subscription for a given user
    ///
    /// @param feedUUID the UUID of the subscription feed
    /// @param userId   the ID of the user
    /// @return a [SubscriptionDTO.UserSubscriptionDTO] containing the updated object
    @Transactional
    public SubscriptionDTO.UserSubscriptionDTO unsubscribeUserFromFeed(UUID feedUUID, Long userId) {
        final var userSubscriptionEntity = userSubscriptionRepository.findByUserIdAndSubscriptionUuid(userId, feedUUID)
                .orElseThrow(() -> new EntityNotFoundException("no subscription found"));

        userSubscriptionEntity.setIsSubscribed(false);
        return userSubscriptionMapper.toDto(userSubscriptionRepository.save(userSubscriptionEntity));
    }
}
