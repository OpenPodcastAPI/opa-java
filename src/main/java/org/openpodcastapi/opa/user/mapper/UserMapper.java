package org.openpodcastapi.opa.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openpodcastapi.opa.user.dto.CreateUserDto;
import org.openpodcastapi.opa.user.dto.UserDto;
import org.openpodcastapi.opa.user.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);

    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subscriptions", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "userRoles", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toEntity(CreateUserDto dto);
}
