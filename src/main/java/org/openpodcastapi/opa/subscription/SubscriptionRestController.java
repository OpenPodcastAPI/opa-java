package org.openpodcastapi.opa.subscription;

import jakarta.persistence.EntityNotFoundException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.openpodcastapi.opa.service.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/// Controller for API subscription requests
@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionRestController {
    private final SubscriptionService service;

    /// Returns all subscriptions for a given user
    ///
    /// @param user                the [CustomUserDetails] of the authenticated user
    /// @param pageable            the [Pageable] pagination object
    /// @param includeUnsubscribed whether to include unsubscribed feeds in the response
    /// @return a [ResponseEntity] containing [SubscriptionDTO.SubscriptionPageDTO] objects
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<SubscriptionDTO.@NonNull SubscriptionPageDTO> getAllSubscriptionsForUser(@AuthenticationPrincipal CustomUserDetails user, Pageable pageable, @RequestParam(defaultValue = "false") boolean includeUnsubscribed) {
        log.info("{}", user.getAuthorities());
        Page<SubscriptionDTO.@NonNull UserSubscriptionDTO> dto;

        if (includeUnsubscribed) {
            dto = service.getAllSubscriptionsForUser(user.id(), pageable);
        } else {
            dto = service.getAllActiveSubscriptionsForUser(user.id(), pageable);
        }

        log.debug("{}", dto);

        return new ResponseEntity<>(SubscriptionDTO.SubscriptionPageDTO.fromPage(dto), HttpStatus.OK);
    }

    /// Returns a single subscription entry by UUID
    ///
    /// @param uuid the UUID value to query for
    /// @param user the [CustomUserDetails] for the user
    /// @return a [ResponseEntity] containing a [SubscriptionDTO.UserSubscriptionDTO] object
    /// @throws EntityNotFoundException  if no entry is found
    /// @throws IllegalArgumentException if the UUID is improperly formatted
    @GetMapping("/{uuid}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<SubscriptionDTO.@NonNull UserSubscriptionDTO> getSubscriptionByUuid(@PathVariable String uuid, @AuthenticationPrincipal CustomUserDetails user) throws EntityNotFoundException {
        // Attempt to validate the UUID value from the provided string
        // If the value is invalid, the GlobalExceptionHandler will throw a 400.
        final var uuidValue = UUID.fromString(uuid);

        // Fetch the subscription, throw an EntityNotFoundException if this fails
        final var dto = service.getUserSubscriptionBySubscriptionUuid(uuidValue, user.id());

        // Return the mapped subscriptionEntity entry
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    /// Updates the subscription status of a subscription for a given user
    ///
    /// @param uuid the UUID of the subscription to update
    /// @param user the [CustomUserDetails] for the user
    /// @return a [ResponseEntity] containing a [SubscriptionDTO.UserSubscriptionDTO] object
    /// @throws EntityNotFoundException  if no entry is found
    /// @throws IllegalArgumentException if the UUID is improperly formatted
    @PostMapping("/{uuid}/unsubscribe")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<SubscriptionDTO.@NonNull UserSubscriptionDTO> unsubscribeUserFromFeed(@PathVariable String uuid, @AuthenticationPrincipal CustomUserDetails user) {
        // Attempt to validate the UUID value from the provided string
        // If the value is invalid, the GlobalExceptionHandler will throw a 400.
        final var uuidValue = UUID.fromString(uuid);

        final var dto = service.unsubscribeUserFromFeed(uuidValue, user.id());

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    /// Bulk creates [UserSubscriptionEntity] objects for a user. Creates new [SubscriptionEntity] objects if not already present
    ///
    /// @param request a list of [SubscriptionDTO.SubscriptionCreateDTO] objects
    /// @param user    the [CustomUserDetails] for the user
    /// @return a [ResponseEntity] containing a [SubscriptionDTO.BulkSubscriptionResponseDTO] object
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<SubscriptionDTO.@NonNull BulkSubscriptionResponseDTO> createUserSubscriptions(@RequestBody List<SubscriptionDTO.SubscriptionCreateDTO> request, @AuthenticationPrincipal CustomUserDetails user) {
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
