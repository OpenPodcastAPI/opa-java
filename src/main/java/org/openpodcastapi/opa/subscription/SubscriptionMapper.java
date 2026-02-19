package org.openpodcastapi.opa.subscription;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/// Mapper for user subscription items
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubscriptionMapper {
    /// Maps a user subscription to a DTO.
    /// Returns the UUID and feed URL of the associated subscription.
    ///
    /// @param subscriptionEntity the entity to map
    /// @return the mapped DTO
    @Mapping(target = "uuid", source = "feed.uuid")
    @Mapping(target = "feedUrl", source = "feed.feedUrl")
    SubscriptionDTO.UserSubscriptionDTO toDto(SubscriptionEntity subscriptionEntity);
}
