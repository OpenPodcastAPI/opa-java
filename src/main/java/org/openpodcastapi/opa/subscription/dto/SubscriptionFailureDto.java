package org.openpodcastapi.opa.subscription.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.UUID;

/// A DTO representing a failed subscription creation
///
/// @param uuid    the UUID of the failed subscription
/// @param feedUrl the feed URL of the failed subscription
/// @param message the error message explaining the failure
public record SubscriptionFailureDto(
        @JsonProperty(value = "uuid", required = true) @UUID String uuid,
        @JsonProperty(value = "feedUrl", required = true) String feedUrl,
        @JsonProperty(value = "message", required = true) String message
) {
}
