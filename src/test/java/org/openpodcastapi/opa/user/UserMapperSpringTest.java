package org.openpodcastapi.opa.user;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.openpodcastapi.opa.user.dto.CreateUserDto;
import org.openpodcastapi.opa.user.mapper.UserMapper;
import org.openpodcastapi.opa.user.model.User;
import org.openpodcastapi.opa.user.repository.UserRepository;
import org.openpodcastapi.opa.user.dto.UserDto;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperSpringTest {
    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void testToDto() {
        final Instant timestamp = Instant.now();
        final UUID uuid = UUID.randomUUID();
        User user = User.builder()
                .uuid(uuid)
                .username("test")
                .email("test@test.test")
                .createdAt(timestamp)
                .updatedAt(timestamp)
                .build();

        UserDto dto = mapper.toDto(user);
        assertNotNull(dto);
        assertEquals(user.getUuid(), dto.uuid());
        assertEquals(user.getUsername(), dto.username());
        assertEquals(user.getEmail(), dto.email());
        assertEquals(user.getCreatedAt(), dto.createdAt());
        assertEquals(user.getUpdatedAt(), dto.updatedAt());
    }

    @Test
    void testToEntity() {
        CreateUserDto dto = new CreateUserDto("test", "testPassword", "test@test.test");
        User user = mapper.toEntity(dto);

        assertNotNull(user);
        assertEquals(dto.email(), user.getEmail());
        assertEquals(dto.username(), user.getUsername());
        assertNull(user.getPassword());
    }
}
