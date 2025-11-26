package org.openpodcastapi.opa.subscription;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/// Mapper for user subscription items
@Mapper(componentModel = "spring")
public interface UserSubscriptionMapper {
    /// Maps a [UserSubscriptionEntity] to a [SubscriptionDTO.UserSubscriptionDTO].
    /// Returns the [SubscriptionEntity#uuid]  and [SubscriptionEntity#feedUrl] of the associated subscription.
    ///
    /// @param userSubscriptionEntity the [UserSubscriptionEntity] to map
    /// @return the mapped [SubscriptionDTO.UserSubscriptionDTO]
    @Mapping(target = "uuid", source = "userSubscriptionEntity.subscription.uuid")
    @Mapping(target = "feedUrl", source = "userSubscriptionEntity.subscription.feedUrl")
    SubscriptionDTO.UserSubscriptionDTO toDto(UserSubscriptionEntity userSubscriptionEntity);
}
