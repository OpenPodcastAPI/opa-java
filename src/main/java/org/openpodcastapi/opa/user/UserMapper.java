package org.openpodcastapi.opa.user;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/// Mapper for user items
@Mapper(componentModel = "spring")
public interface UserMapper {
    /// Maps a [UserEntity] to a [UserDTO.UserResponseDTO]
    ///
    /// @param userEntity the [UserEntity] to map
    /// @return the mapped [UserDTO.UserResponseDTO]
    UserDTO.UserResponseDTO toDto(UserEntity userEntity);

    /// Maps a [UserDTO.CreateUserDTO] to a [UserEntity].
    /// This mapper ignores all fields other than the username and email address.
    /// Other items are populated prior to persistence.
    ///
    /// @param dto the [UserDTO.CreateUserDTO] to map
    /// @return the mapped [UserEntity]
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subscriptions", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "userRoles", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    UserEntity toEntity(UserDTO.CreateUserDTO dto);
}
