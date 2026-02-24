package org.openpodcastapi.opa.subscription;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import org.hibernate.validator.constraints.URL;
import org.hibernate.validator.constraints.UUID;

import java.time.Instant;
import java.util.List;

/// Container for all subscription-related data transfer objects
public class SubscriptionDTO {
    /// A DTO representing a user's subscription to a given feed
    ///
    /// @param uuid           the feed UUID
    /// @param feedUrl        the feed URL
    /// @param createdAt      the date at which the subscription link was created
    /// @param updatedAt      the date at which the subscription link was last updated
    /// @param unsubscribedAt the date at which the user unsubscribed from the feed
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record UserSubscriptionDTO(
            @JsonProperty(required = true) @UUID java.util.UUID uuid,
            @JsonProperty(required = true) @URL String feedUrl,
            @JsonProperty(required = true) Instant createdAt,
            @JsonProperty(required = true) Instant updatedAt,
            @JsonProperty @Nullable Instant unsubscribedAt
    ) {
    }

    /// A DTO representing a bulk subscription creation
    ///
    /// @param success a list of creation successes
    /// @param failure a list of creation failures
    public record BulkSubscriptionResponseDTO(
            List<UserSubscriptionDTO> success,
            List<SubscriptionFailureDTO> failure
    ) {
    }

    /// A DTO representing a failed subscription creation
    ///
    /// @param uuid    the UUID of the failed subscription
    /// @param feedUrl the feed URL of the failed subscription
    /// @param message the error message explaining the failure
    public record SubscriptionFailureDTO(
            @JsonProperty(value = "uuid", required = true) @UUID String uuid,
            @JsonProperty(value = "feedUrl", required = true) String feedUrl,
            @JsonProperty(value = "message", required = true) String message
    ) {
    }
}
