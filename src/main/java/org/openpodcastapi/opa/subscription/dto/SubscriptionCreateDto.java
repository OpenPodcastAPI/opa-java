package org.openpodcastapi.opa.subscription.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.UUID;

/// A DTO representing a new subscription
///
/// @param feedUrl the URL of the feed
/// @param uuid    the UUID of the feed calculated by the client
public record SubscriptionCreateDto(
        @JsonProperty(required = true) @NotNull @UUID String uuid,
        @JsonProperty(required = true) @NotNull String feedUrl
) {
}
