package org.openpodcastapi.opa.subscription.dto;

import java.util.List;

/// A DTO representing a bulk subscription creation
///
/// @param success a list of creation successes
/// @param failure a list of creation failures
public record BulkSubscriptionResponse(
        List<UserSubscriptionDto> success,
        List<SubscriptionFailureDto> failure
) {
}
