package org.openpodcastapi.opa.user;

import jakarta.persistence.EntityNotFoundException;
import org.openpodcastapi.opa.pagination.CursorPage;
import org.openpodcastapi.opa.pagination.CursorRepository;
import org.openpodcastapi.opa.pagination.CursorUtility;
import org.slf4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.slf4j.LoggerFactory.getLogger;

/// Service class for user-related actions
@Service
public class UserService {
    private static final String USER_NOT_FOUND = "User not found";
    private static final Logger log = getLogger(UserService.class);
    private static final QUserEntity qUser = QUserEntity.userEntity;
    private final UserRepository repository;
    private final CursorRepository cursorRepository;
    private final UserMapper mapper;
    private final Argon2PasswordEncoder passwordEncoder;

    /// Required-args constructor
    ///
    /// @param repository       the user repository used for user interactions
    /// @param cursorRepository the cursor repository used for paginated requests
    /// @param mapper           the user mapper used to map user entities and DTOs
    /// @param passwordEncoder  the password encoder used to handle user passwords
    public UserService(UserRepository repository, CursorRepository cursorRepository, UserMapper mapper, Argon2PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.cursorRepository = cursorRepository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
    }

    /// Persists a user to the database
    ///
    /// @param dto the user creation DTO for the user
    /// @return the formatted DTO representation of the user
    /// @throws DataIntegrityViolationException if a user with a matching username or email address exists already
    @Transactional
    public UserDTO.UserResponseDTO createAndPersistUser(UserDTO.CreateUserDTO dto) throws DataIntegrityViolationException {
        // If the user already exists in the system, throw an exception and return a `400` response.
        if (repository.existsUserByEmailOrUsername(dto.email(), dto.username())) {
            throw new DataIntegrityViolationException("User already exists");
        }

        // Create a new user with a hashed password and a default `USER` role.
        final var newUserEntity = mapper.toEntity(dto);
        newUserEntity.setPassword(passwordEncoder.encode(dto.password()));
        newUserEntity.getUserRoles().add(UserRoles.USER);

        // Save the user and return the DTO representation.
        final var persistedUserEntity = repository.save(newUserEntity);
        log.debug("persisted user {}", persistedUserEntity.getUuid());
        return mapper.toDto(persistedUserEntity);
    }

    /// Fetches a cursor-paginated list of all users in the system
    /// Intended for use by admins only
    ///
    /// @param cursor the optional string cursor for offsetting results
    /// @param limit  the number of results to return
    /// @return a cursor page containing user DTOs
    public CursorPage<UserDTO.UserResponseDTO> getAllUsers(String cursor, int limit) {
        // Decode the cursor from the provided string
        final var cursorPayload = cursor == null
                ? null
                : CursorUtility.decode(cursor);

        final var userPage = cursorRepository.findWithCursor(qUser, cursorPayload, limit, null, true);

        return userPage.map(mapper::toDto);
    }

    /// Deletes a user from the database
    ///
    /// @param uuid the UUID of the user to delete
    /// @return a success message
    /// @throws EntityNotFoundException if no matching record is found
    @Transactional
    public String deleteUserAndReturnMessage(UUID uuid) throws EntityNotFoundException {
        final var userEntity = repository.findUserByUuid(uuid).orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));

        repository.delete(userEntity);

        return "user " + uuid.toString() + "deleted";
    }
}
