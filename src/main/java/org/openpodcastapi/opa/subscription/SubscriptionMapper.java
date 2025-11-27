package org.openpodcastapi.opa.subscription;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

/// Mapper for subscription items
@Mapper(componentModel = "spring")
public interface SubscriptionMapper {
    /// Maps a DTO to a subscription entity
    ///
    /// @param dto the DTO to map
    /// @return a mapped subscription entity
    @Mapping(target = "uuid", source = "uuid")
    @Mapping(target = "feedUrl", source = "feedUrl")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SubscriptionEntity toEntity(SubscriptionDTO.SubscriptionCreateDTO dto);

    /// Maps a string UUID to a UUID instance
    ///
    /// @param feedUUID the string UUID to map
    /// @return the mapped UUID instance
    default UUID mapStringToUUID(String feedUUID) {
        return UUID.fromString(feedUUID);
    }
}
