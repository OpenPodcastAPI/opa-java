package org.openpodcastapi.opa.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = UserMapperImpl.class)
class UserMapperSpringTest {
    @Autowired
    private UserMapper mapper;

    @MockitoBean
    private UserRepository userRepository;

    /// Tests that a [User] entity maps to a [UserDTO.UserResponseDTO] representation
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

        UserDTO.UserResponseDTO dto = mapper.toDto(user);
        assertNotNull(dto);
        assertEquals(user.getUuid(), dto.uuid());
        assertEquals(user.getUsername(), dto.username());
        assertEquals(user.getEmail(), dto.email());
        assertEquals(user.getCreatedAt(), dto.createdAt());
        assertEquals(user.getUpdatedAt(), dto.updatedAt());
    }

    /// Tests that a [UserDTO.CreateUserDTO] maps to a [User] entity
    @Test
    void testToEntity() {
        UserDTO.CreateUserDTO dto = new UserDTO.CreateUserDTO("test", "testPassword", "test@test.test");
        User user = mapper.toEntity(dto);

        assertNotNull(user);
        assertEquals(dto.email(), user.getEmail());
        assertEquals(dto.username(), user.getUsername());
        assertNull(user.getPassword());
    }
}
