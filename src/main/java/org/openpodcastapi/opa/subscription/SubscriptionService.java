package org.openpodcastapi.opa.subscription;

import jakarta.persistence.EntityNotFoundException;
import org.jspecify.annotations.NonNull;
import org.openpodcastapi.opa.feed.FeedDTO;
import org.openpodcastapi.opa.feed.FeedEntity;
import org.openpodcastapi.opa.feed.FeedService;
import org.openpodcastapi.opa.pagination.CursorPage;
import org.openpodcastapi.opa.pagination.CursorRepository;
import org.openpodcastapi.opa.pagination.CursorUtility;
import org.openpodcastapi.opa.user.UserRepository;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.slf4j.LoggerFactory.getLogger;

/// Service for subscription-related actions
@Service
public class SubscriptionService {
    private static final Logger log = getLogger(SubscriptionService.class);
    private static final QSubscriptionEntity qSubscription = QSubscriptionEntity.subscriptionEntity;
    private final FeedService feedService;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper mapper;
    private final UserRepository userRepository;
    private final CursorRepository cursorRepository;

    /// All-args constructor
    ///
    /// @param feedService            the repository used for feed interactions
    /// @param subscriptionRepository the repository used for user subscription interactions
    /// @param mapper                 the mapper used for mapping user subscription entities and DTOs
    /// @param userRepository         the repository used for user interactions
    /// @param cursorRepository       the cursor repository used for paginated requests
    public SubscriptionService(FeedService feedService, SubscriptionRepository subscriptionRepository, SubscriptionMapper mapper, UserRepository userRepository, CursorRepository cursorRepository) {
        this.feedService = feedService;
        this.subscriptionRepository = subscriptionRepository;
        this.mapper = mapper;
        this.userRepository = userRepository;
        this.cursorRepository = cursorRepository;
    }

    /// Fetches a single subscription for an authenticated userEntity, if it exists
    ///
    /// @param subscriptionUuid the UUID of the subscription
    /// @param userId           the database ID of the user
    /// @return a DTO of the user subscription
    /// @throws EntityNotFoundException if no entry is found
    @Transactional(readOnly = true)
    public SubscriptionDTO.UserSubscriptionDTO getUserSubscriptionBySubscriptionUuid(UUID subscriptionUuid, Long userId) throws EntityNotFoundException {
        log.debug("Fetching subscription {} for userEntity {}", subscriptionUuid, userId);
        final var userSubscription = subscriptionRepository.findByUserIdAndFeedUuid(userId, subscriptionUuid)
                .orElseThrow(() -> new EntityNotFoundException("subscription not found for userEntity"));

        log.debug("Subscription {} for userEntity {} found", subscriptionUuid, userId);
        return mapper.toDto(userSubscription);
    }

    /// Gets all subscriptions for the authenticated user
    ///
    /// @param userId              the database ID of the authenticated user
    /// @param cursor              the encoded cursor
    /// @param limit               the number of results to return
    /// @param includeUnsubscribed whether to return unsubscribed results as well
    /// @return a paginated set of user subscriptions
    @Transactional(readOnly = true)
    public CursorPage<SubscriptionDTO.UserSubscriptionDTO> getAllSubscriptionsForUser(Long userId, String cursor, int limit, boolean includeUnsubscribed) {
        log.debug("Fetching subscriptions for {}", userId);
        // Decode the cursor from the provided string
        final var cursorPayload = cursor == null
                ? null
                : CursorUtility.decode(cursor);

        // Create a filter for the user ID
        var filter = qSubscription.user.id.eq(userId);

        // If the user hasn't requested unsubscribed records, only return those where the timestamp is null
        if (!includeUnsubscribed) {
            filter = filter.and(qSubscription.unsubscribedAt.isNull());
        }

        // Fetch and build the page
        final var subscriptionPage = cursorRepository.findWithCursor(qSubscription, cursorPayload, limit, filter, true);

        return subscriptionPage.map(mapper::toDto);
    }

    /// Persists a new user subscription to the database
    /// If an existing entry is found for the user and subscription, the `isSubscribed` property is set to `true`
    ///
    /// @param feed   the target feed
    /// @param userId the ID of the target user
    /// @return a response containing a user subscription DTO
    /// @throws EntityNotFoundException if no matching user is found
    protected SubscriptionDTO.UserSubscriptionDTO persistUserSubscription(FeedEntity feed, Long userId) {
        final var userEntity = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("user not found"));

        log.debug("{}", userEntity);

        final var newSubscription = subscriptionRepository.findByUserIdAndFeedUuid(userId, feed.getUuid()).orElseGet(() -> {
            log.debug("Creating new subscription for user {} and subscription {}", userId, feed.getUuid());
            final var createdSubscriptionEntity = new SubscriptionEntity();
            createdSubscriptionEntity.setUnsubscribedAt(null);
            createdSubscriptionEntity.setUser(userEntity);
            createdSubscriptionEntity.setFeed(feed);
            return subscriptionRepository.save(createdSubscriptionEntity);
        });

        newSubscription.setUnsubscribedAt(null);
        return mapper.toDto(subscriptionRepository.save(newSubscription));
    }

    /// Creates user subscriptions in bulk. If the subscription isn't already in the system, this is added before the user is subscribed.
    ///
    /// @param requests a list of subscriptions to create
    /// @param userId   the ID of the requesting user
    /// @return a response containing a bulk creation DTO
    @Transactional
    public SubscriptionDTO.@NonNull BulkSubscriptionResponseDTO addSubscriptions(List<FeedDTO.NewFeedRequestDTO> requests, Long userId) {
        List<SubscriptionDTO.UserSubscriptionDTO> successes = new ArrayList<>();
        List<SubscriptionDTO.SubscriptionFailureDTO> failures = new ArrayList<>();

        log.info("{}", requests);

        for (var subscriptionObject : requests) {
            try {
                // Fetch or create the subscription object to subscribe the user to
                final var feed = this.feedService.fetchOrCreateFeed(subscriptionObject);
                // If all is successful, persist the new SubscriptionEntity and add a UserSubscriptionDTO to the successes list
                successes.add(persistUserSubscription(feed, userId));
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
    /// @return a response containing the updated subscription
    @Transactional
    public SubscriptionDTO.UserSubscriptionDTO unsubscribeUserFromFeed(UUID feedUUID, Long userId) {
        final var userSubscriptionEntity = subscriptionRepository.findByUserIdAndFeedUuid(userId, feedUUID)
                .orElseThrow(() -> new EntityNotFoundException("no subscription found"));

        userSubscriptionEntity.setUnsubscribedAt(Instant.now());
        return mapper.toDto(subscriptionRepository.save(userSubscriptionEntity));
    }
}
