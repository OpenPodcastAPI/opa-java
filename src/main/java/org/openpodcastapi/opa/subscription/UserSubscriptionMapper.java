package org.openpodcastapi.opa.subscription;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/// Mapper for user subscription items
@Mapper(componentModel = "spring")
public interface UserSubscriptionMapper {
    /// Maps a user subscription to a DTO.
    /// Returns the UUID and feed URL of the associated subscription.
    ///
    /// @param userSubscriptionEntity the entity to map
    /// @return the mapped DTO
    @Mapping(target = "uuid", source = "subscription.uuid")
    @Mapping(target = "feedUrl", source = "subscription.feedUrl")
    SubscriptionDTO.UserSubscriptionDTO toDto(UserSubscriptionEntity userSubscriptionEntity);
}
