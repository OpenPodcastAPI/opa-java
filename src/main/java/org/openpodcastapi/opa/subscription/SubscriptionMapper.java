package org.openpodcastapi.opa.subscription;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", source = "uuid")
    @Mapping(target = "subscribers", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SubscriptionEntity toEntity(SubscriptionDTO.SubscriptionCreateDTO dto);

    default UUID mapStringToUUID(String feedUUID) {
        return UUID.fromString(feedUUID);
    }
}
