package org.openpodcastapi.opa.subscription.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.URL;
import org.hibernate.validator.constraints.UUID;

import java.time.Instant;

/// A DTO representing a user's subscription to a given feed
///
/// @param uuid         the feed UUID
/// @param feedUrl      the feed URL
/// @param createdAt    the date at which the subscription link was created
/// @param updatedAt    the date at which the subscription link was last updated
/// @param isSubscribed whether the user is currently subscribed to the feed
public record UserSubscriptionDto(
        @JsonProperty(required = true) @UUID java.util.UUID uuid,
        @JsonProperty(required = true) @URL String feedUrl,
        @JsonProperty(required = true) Instant createdAt,
        @JsonProperty(required = true) Instant updatedAt,
        @JsonProperty(required = true) Boolean isSubscribed
) {
}
