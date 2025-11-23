package org.openpodcastapi.opa.subscription;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserSubscriptionMapper {
    @Mapping(target = "uuid", source = "userSubscriptionEntity.subscription.uuid")
    @Mapping(target = "feedUrl", source = "userSubscriptionEntity.subscription.feedUrl")
    SubscriptionDTO.UserSubscriptionDTO toDto(UserSubscriptionEntity userSubscriptionEntity);
}
