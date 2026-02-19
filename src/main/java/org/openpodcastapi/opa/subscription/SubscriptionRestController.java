package org.openpodcastapi.opa.subscription;

import jakarta.persistence.EntityNotFoundException;
import org.jspecify.annotations.NonNull;
import org.openpodcastapi.opa.feed.FeedDTO;
import org.openpodcastapi.opa.service.CustomUserDetails;
import org.slf4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static org.slf4j.LoggerFactory.getLogger;

/// Controller for API subscription requests
@RestController
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionRestController {
    private static final Logger log = getLogger(SubscriptionRestController.class);
    private final SubscriptionService service;

    /// Required-args constructor
    ///
    /// @param service the service used for subscription actions
    public SubscriptionRestController(SubscriptionService service) {
        this.service = service;
    }

    /// Returns all subscriptions for a given user
    ///
    /// @param user                the custom user details of the authenticated user
    /// @param pageable            the pagination options
    /// @param includeUnsubscribed whether to include unsubscribed feed in the response
    /// @return a response containing subscription objects
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<SubscriptionDTO.@NonNull SubscriptionPageDTO> getAllSubscriptionsForUser(@AuthenticationPrincipal CustomUserDetails user, Pageable pageable, @RequestParam(defaultValue = "false") boolean includeUnsubscribed) {
        log.info("{}", user.getAuthorities());
        final Page<SubscriptionDTO.@NonNull UserSubscriptionDTO> dto;

        if (includeUnsubscribed) {
            dto = service.getAllSubscriptionsForUser(user.id(), pageable);
        } else {
            dto = service.getAllActiveSubscriptionsForUser(user.id(), pageable);
        }

        return new ResponseEntity<>(SubscriptionDTO.SubscriptionPageDTO.fromPage(dto), HttpStatus.OK);
    }

    /// Returns a single subscription entry by UUID
    ///
    /// @param uuid the UUID value to query for
    /// @param user the custom user details for the user
    /// @return a response containing a subscription DTO
    /// @throws EntityNotFoundException  if no entry is found
    /// @throws IllegalArgumentException if the UUID is improperly formatted
    @GetMapping("/{uuid}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<SubscriptionDTO.@NonNull UserSubscriptionDTO> getSubscriptionByUuid(@PathVariable String uuid, @AuthenticationPrincipal CustomUserDetails user) throws EntityNotFoundException {
        // Attempt to validate the UUID value from the provided string
        // If the value is invalid, the GlobalExceptionHandler will throw a 400.
        final var uuidValue = UUID.fromString(uuid);

        // Fetch the subscription, throw an EntityNotFoundException if this fails
        final var dto = service.getUserSubscriptionBySubscriptionUuid(uuidValue, user.id());

        // Return the mapped subscription entry
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    /// Updates the subscription status of a subscription for a given user
    ///
    /// @param uuid the UUID of the subscription to update
    /// @param user the custom user details for the user
    /// @return a reponse containing a subscription DTO
    /// @throws EntityNotFoundException  if no entry is found
    /// @throws IllegalArgumentException if the UUID is improperly formatted
    @PostMapping("/{uuid}/unsubscribe")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<SubscriptionDTO.@NonNull UserSubscriptionDTO> unsubscribeUserFromFeed(@PathVariable String uuid, @AuthenticationPrincipal CustomUserDetails user) {
        // Attempt to validate the UUID value from the provided string
        // If the value is invalid, the GlobalExceptionHandler will throw a 400.
        final var uuidValue = UUID.fromString(uuid);

        final var dto = service.unsubscribeUserFromFeed(uuidValue, user.id());

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    /// Bulk creates user subscriptions for a user. Creates new subscriptions if not already present
    ///
    /// @param request a list of subscription creation DTOs
    /// @param user    the custom user details for the user
    /// @return a response containing a bulk subscription DTO
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<SubscriptionDTO.@NonNull BulkSubscriptionResponseDTO> createUserSubscriptions(@RequestBody List<FeedDTO.NewFeedRequestDTO> request, @AuthenticationPrincipal CustomUserDetails user) {
        final var response = service.addSubscriptions(request, user.id());

        if (response.success().isEmpty() && !response.failure().isEmpty()) {
            // If all requests failed, return a 400 error
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } else if (!response.success().isEmpty() && !response.failure().isEmpty()) {
            // If some requests succeeded and some failed, return a 207
            return new ResponseEntity<>(response, HttpStatus.MULTI_STATUS);
        } else {
            // If all requests succeeded, return a 200
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }
}
