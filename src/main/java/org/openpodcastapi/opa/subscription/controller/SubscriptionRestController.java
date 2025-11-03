package org.openpodcastapi.opa.subscription.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.openpodcastapi.opa.service.CustomUserDetails;
import org.openpodcastapi.opa.subscription.dto.BulkSubscriptionResponse;
import org.openpodcastapi.opa.subscription.dto.SubscriptionCreateDto;
import org.openpodcastapi.opa.subscription.dto.SubscriptionPageDto;
import org.openpodcastapi.opa.subscription.dto.UserSubscriptionDto;
import org.openpodcastapi.opa.subscription.service.UserSubscriptionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionRestController {
    private final UserSubscriptionService service;

    /// Returns all subscriptions for a given user
    ///
    /// @param user     the [CustomUserDetails] of the authenticated user
    /// @param pageable the [Pageable] pagination object
    /// @return a paginated list of subscriptions
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<SubscriptionPageDto> getAllSubscriptionsForUser(@AuthenticationPrincipal CustomUserDetails user, Pageable pageable) {
        Page<UserSubscriptionDto> dto = service.getAllSubscriptionsForUser(user.id(), pageable);

        return new ResponseEntity<>(SubscriptionPageDto.fromPage(dto), HttpStatus.OK);
    }

    /// Returns a single subscription entry by UUID
    ///
    /// @param uuid the UUID value to query for
    /// @return the subscription entity
    /// @throws EntityNotFoundException if no entry is found
    @GetMapping("/{uuid}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<UserSubscriptionDto> getSubscriptionByUuid(@PathVariable String uuid, @AuthenticationPrincipal CustomUserDetails user) throws EntityNotFoundException {
        // Attempt to validate the UUID value from the provided string
        // If the value is invalid, the GlobalExceptionHandler will throw a 400.
        UUID uuidValue = UUID.fromString(uuid);

        // Fetch the subscription, throw an EntityNotFoundException if this fails
        UserSubscriptionDto dto = service.getUserSubscriptionBySubscriptionUuid(uuidValue, user.id());

        // Return the mapped subscription entry
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    /// Bulk creates UserSubscriptions for a user. Creates new Subscription objects if not already present
    ///
    /// @param request a list of [SubscriptionCreateDto] objects
    /// @return a [BulkSubscriptionResponse] object
    @PostMapping
    public ResponseEntity<BulkSubscriptionResponse> createUserSubscriptions(@RequestBody List<SubscriptionCreateDto> request, @AuthenticationPrincipal CustomUserDetails user) {
        BulkSubscriptionResponse response = service.addSubscriptions(request, user.id());

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
