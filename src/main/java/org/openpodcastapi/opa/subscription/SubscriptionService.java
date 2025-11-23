package org.openpodcastapi.opa.subscription;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.openpodcastapi.opa.user.UserEntity;
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
    /// @param dto the [SubscriptionDTO.SubscriptionCreateDTO] containing the subscriptionEntity data
    /// @return the fetched or created [SubscriptionEntity]
    protected SubscriptionEntity fetchOrCreateSubscription(SubscriptionDTO.SubscriptionCreateDTO dto) {
        UUID feedUuid = UUID.fromString(dto.uuid());
        return subscriptionRepository
                .findByUuid(feedUuid)
                .orElseGet(() -> {
                    log.debug("Creating new subscriptionEntity with UUID {}", dto.uuid());
                    return subscriptionRepository.save(subscriptionMapper.toEntity(dto));
                });
    }

    /// Fetches a single subscriptionEntity for an authenticated userEntity, if it exists
    ///
    /// @param subscriptionUuid the UUID of the subscriptionEntity
    /// @param userId           the database ID of the userEntity
    /// @return a [SubscriptionDTO.UserSubscriptionDTO] of the userEntity subscriptionEntity
    /// @throws EntityNotFoundException if no entry is found
    @Transactional(readOnly = true)
    public SubscriptionDTO.UserSubscriptionDTO getUserSubscriptionBySubscriptionUuid(UUID subscriptionUuid, Long userId) {
        log.debug("Fetching subscriptionEntity {} for userEntity {}", subscriptionUuid, userId);
        UserSubscriptionEntity subscription = userSubscriptionRepository.findByUserIdAndSubscriptionUuid(userId, subscriptionUuid)
                .orElseThrow(() -> new EntityNotFoundException("subscriptionEntity not found for userEntity"));

        log.debug("SubscriptionEntity {} for userEntity {} found", subscriptionUuid, userId);
        return userSubscriptionMapper.toDto(subscription);
    }

    /// Gets all subscriptions for the authenticated userEntity
    ///
    /// @param userId the database ID of the authenticated userEntity
    /// @return a paginated set of [SubscriptionDTO.UserSubscriptionDTO] objects
    @Transactional(readOnly = true)
    public Page<SubscriptionDTO.UserSubscriptionDTO> getAllSubscriptionsForUser(Long userId, Pageable pageable) {
        log.debug("Fetching subscriptions for {}", userId);
        return userSubscriptionRepository.findAllByUserId(userId, pageable)
                .map(userSubscriptionMapper::toDto);
    }

    /// Gets all active subscriptions for the authenticated userEntity
    ///
    /// @param userId the database ID of the authenticated userEntity
    /// @return a paginated set of [SubscriptionDTO.UserSubscriptionDTO] objects
    @Transactional(readOnly = true)
    public Page<SubscriptionDTO.UserSubscriptionDTO> getAllActiveSubscriptionsForUser(Long userId, Pageable pageable) {
        log.debug("Fetching all active subscriptions for {}", userId);
        return userSubscriptionRepository.findAllByUserIdAndIsSubscribedTrue(userId, pageable).map(userSubscriptionMapper::toDto);
    }

    /// Persists a new userEntity subscriptionEntity to the database
    /// If an existing entry is found for the userEntity and subscriptionEntity, the `isSubscribed` property is set to `true`
    ///
    /// @param subscriptionEntity the target subscriptionEntity
    /// @param userId             the ID of the target userEntity
    /// @return a [SubscriptionDTO.UserSubscriptionDTO] representation of the subscriptionEntity link
    /// @throws EntityNotFoundException if no matching userEntity is found
    protected SubscriptionDTO.UserSubscriptionDTO persistUserSubscription(SubscriptionEntity subscriptionEntity, Long userId) {
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("userEntity not found"));
        log.debug("{}", userEntity);

        UserSubscriptionEntity newSubscription = userSubscriptionRepository.findByUserIdAndSubscriptionUuid(userId, subscriptionEntity.getUuid()).orElseGet(() -> {
            log.debug("Creating new userEntity subscriptionEntity for userEntity {} and subscriptionEntity {}", userId, subscriptionEntity.getUuid());
            UserSubscriptionEntity createdSubscription = new UserSubscriptionEntity();
            createdSubscription.setIsSubscribed(true);
            createdSubscription.setUser(userEntity);
            createdSubscription.setSubscription(subscriptionEntity);
            return userSubscriptionRepository.save(createdSubscription);
        });

        newSubscription.setIsSubscribed(true);
        return userSubscriptionMapper.toDto(userSubscriptionRepository.save(newSubscription));
    }

    /// Creates UserSubscriptionEntity links in bulk. If the SubscriptionEntity isn't already in the system, this is added before the userEntity is subscribed.
    ///
    /// @param requests a list of [SubscriptionDTO.SubscriptionCreateDTO] objects to create
    /// @param userId   the ID of the requesting userEntity
    /// @return a [SubscriptionDTO.BulkSubscriptionResponseDTO] DTO containing a list of successes and failures
    @Transactional
    public SubscriptionDTO.BulkSubscriptionResponseDTO addSubscriptions(List<SubscriptionDTO.SubscriptionCreateDTO> requests, Long userId) {
        List<SubscriptionDTO.UserSubscriptionDTO> successes = new ArrayList<>();
        List<SubscriptionDTO.SubscriptionFailureDTO> failures = new ArrayList<>();

        log.info("{}", requests);

        for (SubscriptionDTO.SubscriptionCreateDTO dto : requests) {
            try {
                // Fetch or create the subscriptionEntity object to subscribe the userEntity to
                SubscriptionEntity subscriptionEntity = this.fetchOrCreateSubscription(dto);
                log.debug("{}", subscriptionEntity);
                // If all is successful, persist the new UserSubscriptionEntity and add a UserSubscriptionDTO to the successes list
                successes.add(persistUserSubscription(subscriptionEntity, userId));
            } catch (IllegalArgumentException _) {
                // If the UUID of the feed is invalid, add a new failure to the failures list
                failures.add(new SubscriptionDTO.SubscriptionFailureDTO(dto.uuid(), dto.feedUrl(), "invalid UUID format"));
            } catch (Exception e) {
                // If another failure is encountered, add it to the failures list
                failures.add(new SubscriptionDTO.SubscriptionFailureDTO(dto.uuid(), dto.feedUrl(), e.getMessage()));
            }
        }

        // Return the entire DTO of successes and failures
        return new SubscriptionDTO.BulkSubscriptionResponseDTO(successes, failures);
    }

    /// Updates the status of a subscriptionEntity for a given userEntity
    ///
    /// @param feedUUID the UUID of the subscriptionEntity feed
    /// @param userId   the ID of the userEntity
    /// @return a [SubscriptionDTO.UserSubscriptionDTO] containing the updated object
    @Transactional
    public SubscriptionDTO.UserSubscriptionDTO unsubscribeUserFromFeed(UUID feedUUID, Long userId) {
        UserSubscriptionEntity subscription = userSubscriptionRepository.findByUserIdAndSubscriptionUuid(userId, feedUUID)
                .orElseThrow(() -> new EntityNotFoundException("no subscriptionEntity found"));

        subscription.setIsSubscribed(false);
        return userSubscriptionMapper.toDto(userSubscriptionRepository.save(subscription));
    }
}
