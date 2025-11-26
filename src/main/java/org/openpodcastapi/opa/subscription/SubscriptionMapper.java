package org.openpodcastapi.opa.subscription;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

/// Mapper for subscription items
@Mapper(componentModel = "spring")
public interface SubscriptionMapper {
    /// Maps a [SubscriptionDTO.SubscriptionCreateDTO] to a [SubscriptionEntity]
    ///
    /// @param dto the [SubscriptionDTO.SubscriptionCreateDTO] to map
    /// @return a mapped [SubscriptionEntity]
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", source = "uuid")
    @Mapping(target = "subscribers", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SubscriptionEntity toEntity(SubscriptionDTO.SubscriptionCreateDTO dto);

    /// Maps a string UUID to a UUID instance
    ///
    /// @param feedUUID the string UUID to map
    /// @return the mapped [UUID] instance
    default UUID mapStringToUUID(String feedUUID) {
        return UUID.fromString(feedUUID);
    }
}
