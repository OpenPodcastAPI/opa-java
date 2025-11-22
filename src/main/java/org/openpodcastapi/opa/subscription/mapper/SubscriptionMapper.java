package org.openpodcastapi.opa.subscription.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openpodcastapi.opa.subscription.dto.SubscriptionCreateDto;
import org.openpodcastapi.opa.subscription.model.Subscription;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", source = "uuid")
    @Mapping(target = "subscribers", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Subscription toEntity(SubscriptionCreateDto dto);

    default UUID mapStringToUUID(String feedUUID) {
        return UUID.fromString(feedUUID);
    }
}
