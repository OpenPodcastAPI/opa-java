package org.openpodcastapi.opa.subscription.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.openpodcastapi.opa.subscription.dto.BulkSubscriptionResponse;
import org.openpodcastapi.opa.subscription.dto.SubscriptionCreateDto;
import org.openpodcastapi.opa.subscription.dto.SubscriptionFailureDto;
import org.openpodcastapi.opa.subscription.dto.UserSubscriptionDto;
import org.openpodcastapi.opa.subscription.mapper.UserSubscriptionMapper;
import org.openpodcastapi.opa.subscription.model.Subscription;
import org.openpodcastapi.opa.subscription.model.UserSubscription;
import org.openpodcastapi.opa.subscription.repository.UserSubscriptionRepository;
import org.openpodcastapi.opa.user.model.User;
import org.openpodcastapi.opa.user.repository.UserRepository;
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
public class UserSubscriptionService {
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final UserSubscriptionMapper userSubscriptionMapper;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;

    /// Fetches a single subscription for an authenticated user, if it exists
    ///
    /// @param subscriptionUuid the UUID of the subscription
    /// @param userId           the database ID of the user
    /// @return a [UserSubscriptionDto] of the user subscription
    /// @throws EntityNotFoundException if no entry is found
    @Transactional(readOnly = true)
    public UserSubscriptionDto getUserSubscriptionBySubscriptionUuid(UUID subscriptionUuid, Long userId) {
        log.debug("Fetching subscription {} for user {}", subscriptionUuid, userId);
        UserSubscription subscription = userSubscriptionRepository.findByUserIdAndSubscriptionUuid(userId, subscriptionUuid)
                .orElseThrow(() -> new EntityNotFoundException("subscription not found for user"));

        log.debug("Subscription {} for user {} found", subscriptionUuid, userId);
        return userSubscriptionMapper.toDto(subscription);
    }

    /// Gets all subscriptions for the authenticated user
    ///
    /// @param userId the database ID of the authenticated user
    /// @return a paginated set of [UserSubscriptionDto] objects
    @Transactional(readOnly = true)
    public Page<UserSubscriptionDto> getAllSubscriptionsForUser(Long userId, Pageable pageable) {
        log.debug("Fetching subscriptions for {}", userId);
        return userSubscriptionRepository.findAllByUserId(userId, pageable)
                .map(userSubscriptionMapper::toDto);
    }

    /// Gets all active subscriptions for the authenticated user
    ///
    /// @param userId the database ID of the authenticated user
    /// @return a paginated set of [UserSubscriptionDto] objects
    @Transactional(readOnly = true)
    public Page<UserSubscriptionDto> getAllActiveSubscriptionsForUser(Long userId, Pageable pageable) {
        log.debug("Fetching all active subscriptions for {}", userId);
        return userSubscriptionRepository.findAllByUserIdAndIsSubscribedTrue(userId, pageable).map(userSubscriptionMapper::toDto);
    }

    /// Persists a new user subscription to the database
    /// If an existing entry is found for the user and subscription, the `isSubscribed` property is set to `true`
    ///
    /// @param subscription the target subscription
    /// @param userId       the ID of the target user
    /// @return a [UserSubscriptionDto] representation of the subscription link
    /// @throws EntityNotFoundException if no matching user is found
    protected UserSubscriptionDto persistUserSubscription(Subscription subscription, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("user not found"));
        log.debug("{}", user);

        UserSubscription newSubscription = userSubscriptionRepository.findByUserIdAndSubscriptionUuid(userId, subscription.getUuid()).orElseGet(() -> {
            log.debug("Creating new user subscription for user {} and subscription {}", userId, subscription.getUuid());
            UserSubscription createdSubscription = new UserSubscription();
            createdSubscription.setIsSubscribed(true);
            createdSubscription.setUser(user);
            createdSubscription.setSubscription(subscription);
            return userSubscriptionRepository.save(createdSubscription);
        });

        newSubscription.setIsSubscribed(true);
        return userSubscriptionMapper.toDto(userSubscriptionRepository.save(newSubscription));
    }

    /// Creates UserSubscription links in bulk. If the Subscription isn't already in the system, this is added before the user is subscribed.
    ///
    /// @param requests a list of [SubscriptionCreateDto] objects to create
    /// @param userId   the ID of the requesting user
    /// @return a [BulkSubscriptionResponse] DTO containing a list of successes and failures
    @Transactional
    public BulkSubscriptionResponse addSubscriptions(List<SubscriptionCreateDto> requests, Long userId) {
        List<UserSubscriptionDto> successes = new ArrayList<>();
        List<SubscriptionFailureDto> failures = new ArrayList<>();

        log.info("{}", requests);

        for (SubscriptionCreateDto dto : requests) {
            try {
                // Fetch or create the subscription object to subscribe the user to
                Subscription subscription = subscriptionService.fetchOrCreateSubscription(dto);
                log.debug("{}", subscription);
                // If all is successful, persist the new UserSubscription and add a UserSubscriptionDto to the successes list
                successes.add(persistUserSubscription(subscription, userId));
            } catch (IllegalArgumentException _) {
                // If the UUID of the feed is invalid, add a new failure to the failures list
                failures.add(new SubscriptionFailureDto(dto.uuid(), dto.feedUrl(), "invalid UUID format"));
            } catch (Exception e) {
                // If another failure is encountered, add it to the failures list
                failures.add(new SubscriptionFailureDto(dto.uuid(), dto.feedUrl(), e.getMessage()));
            }
        }

        // Return the entire DTO of successes and failures
        return new BulkSubscriptionResponse(successes, failures);
    }

    /// Updates the status of a subscription for a given user
    ///
    /// @param feedUUID the UUID of the subscription feed
    /// @param userId   the ID of the user
    /// @return a [UserSubscriptionDto] containing the updated object
    @Transactional
    public UserSubscriptionDto unsubscribeUserFromFeed(UUID feedUUID, Long userId) {
        UserSubscription subscription = userSubscriptionRepository.findByUserIdAndSubscriptionUuid(userId, feedUUID)
                .orElseThrow(() -> new EntityNotFoundException("no subscription found"));

        subscription.setIsSubscribed(false);
        return userSubscriptionMapper.toDto(userSubscriptionRepository.save(subscription));
    }
}
