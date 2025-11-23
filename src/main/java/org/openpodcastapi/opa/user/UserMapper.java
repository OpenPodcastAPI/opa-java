package org.openpodcastapi.opa.user;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO.UserResponseDTO toDto(User user);

    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subscriptions", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "userRoles", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toEntity(UserDTO.CreateUserDTO dto);
}
