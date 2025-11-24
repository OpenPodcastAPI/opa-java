package org.openpodcastapi.opa.subscription;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import org.hibernate.validator.constraints.URL;
import org.hibernate.validator.constraints.UUID;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.List;

public class SubscriptionDTO {
    /// A DTO representing a new subscription
    ///
    /// @param feedUrl the URL of the feed
    /// @param uuid    the UUID of the feed calculated by the client
    public record SubscriptionCreateDTO(
            @JsonProperty(required = true) @NotNull @UUID String uuid,
            @JsonProperty(required = true) @NotNull String feedUrl
    ) {
    }

    /// A DTO representing a user's subscription to a given feed
    ///
    /// @param uuid           the feed UUID
    /// @param feedUrl        the feed URL
    /// @param createdAt      the date at which the subscription link was created
    /// @param updatedAt      the date at which the subscription link was last updated
    /// @param unsubscribedAt the date at which the user unsubscribed from the feed
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

    /// A paginated DTO representing a list of subscriptions
    ///
    /// @param subscriptions    the [UserSubscriptionDTO] list representing the subscriptions
    /// @param first            whether this is the first page
    /// @param last             whether this is the last page
    /// @param page             the current page number
    /// @param totalPages       the total number of pages in the result set
    /// @param numberOfElements the number of elements in the current page
    /// @param totalElements    the total number of elements in the result set
    /// @param size             the size limit applied to the page
    public record SubscriptionPageDTO(
            List<UserSubscriptionDTO> subscriptions,
            boolean first,
            boolean last,
            int page,
            int totalPages,
            long totalElements,
            int numberOfElements,
            int size
    ) {
        public static SubscriptionPageDTO fromPage(Page<@NonNull UserSubscriptionDTO> page) {
            return new SubscriptionPageDTO(
                    page.getContent(),
                    page.isFirst(),
                    page.isLast(),
                    page.getNumber(),
                    page.getTotalPages(),
                    page.getTotalElements(),
                    page.getNumberOfElements(),
                    page.getSize()
            );
        }
    }
}
