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
class UserMapperTest {
    @Autowired
    private UserMapper mapper;

    @MockitoBean
    private UserRepository userRepository;

    /// Tests that a [UserEntity] entity maps to a [UserDTO.UserResponseDTO] representation
    @Test
    void testToDto() {
        final Instant timestamp = Instant.now();
        final UUID uuid = UUID.randomUUID();
        final var userEntity = new UserEntity(1L, uuid, "test", "", "test@test.test", timestamp, timestamp);

        UserDTO.UserResponseDTO dto = mapper.toDto(userEntity);
        assertNotNull(dto);
        assertEquals(userEntity.getUuid(), dto.uuid());
        assertEquals(userEntity.getUsername(), dto.username());
        assertEquals(userEntity.getEmail(), dto.email());
        assertEquals(userEntity.getCreatedAt(), dto.createdAt());
        assertEquals(userEntity.getUpdatedAt(), dto.updatedAt());
    }

    /// Tests that a [UserDTO.CreateUserDTO] maps to a [UserEntity] entity
    @Test
    void testToEntity() {
        UserDTO.CreateUserDTO dto = new UserDTO.CreateUserDTO("test", "testPassword", "test@test.test");
        UserEntity userEntity = mapper.toEntity(dto);

        assertNotNull(userEntity);
        assertEquals(dto.email(), userEntity.getEmail());
        assertEquals(dto.username(), userEntity.getUsername());
        assertNull(userEntity.getPassword());
    }
}
