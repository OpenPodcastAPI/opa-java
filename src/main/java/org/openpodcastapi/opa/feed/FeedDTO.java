package org.openpodcastapi.opa.feed;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.UUID;

/// DTO records for feed entities
public class FeedDTO {
    /// A DTO representing a new subscription
    ///
    /// @param feedUrl the URL of the feed
    /// @param uuid    the UUID of the feed calculated by the client
    public record NewFeedRequestDTO(
            @JsonProperty(required = true) @NotNull @UUID String uuid,
            @JsonProperty(required = true) @NotNull String feedUrl
    ) {
    }
}
