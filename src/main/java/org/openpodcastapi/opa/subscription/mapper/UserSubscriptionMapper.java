package org.openpodcastapi.opa.subscription.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openpodcastapi.opa.subscription.dto.UserSubscriptionDto;
import org.openpodcastapi.opa.subscription.model.UserSubscription;

@Mapper(componentModel = "spring")
public interface UserSubscriptionMapper {
    @Mapping(target = "uuid", source = "userSubscription.subscription.uuid")
    @Mapping(target = "feedUrl", source = "userSubscription.subscription.feedUrl")
    UserSubscriptionDto toDto(UserSubscription userSubscription);
}
