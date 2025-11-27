package org.openpodcastapi.opa.user;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/// Mapper for user items
@Mapper(componentModel = "spring")
public interface UserMapper {
    /// Maps a user entity to a DTO.
    ///
    /// @param userEntity the entity to map
    /// @return the mapped DTO
    UserDTO.UserResponseDTO toDto(UserEntity userEntity);

    /// Maps a user creation DTO to an entity.
    /// This mapper ignores all fields other than the username and email address.
    /// Other items are populated prior to persistence.
    ///
    /// @param dto the user creation DTO to map
    /// @return the mapped entity
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "subscriptions", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "userRoles", ignore = true)
    UserEntity toEntity(UserDTO.CreateUserDTO dto);
}
