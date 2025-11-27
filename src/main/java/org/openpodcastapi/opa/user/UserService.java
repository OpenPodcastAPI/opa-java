package org.openpodcastapi.opa.user;

import jakarta.persistence.EntityNotFoundException;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/// Service class for user-related actions
@Service
public class UserService {
    private static final String USER_NOT_FOUND = "User not found";
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(UserService.class);
    private final UserRepository repository;
    private final UserMapper mapper;
    private final BCryptPasswordEncoder passwordEncoder;

    /// Required-args constructor
    ///
    /// @param repository      the user repository used for user interactions
    /// @param mapper          the user mapper used to map user entities and DTOs
    /// @param passwordEncoder the password encoder used to handle user passwords
    public UserService(UserRepository repository, UserMapper mapper, BCryptPasswordEncoder passwordEncoder) {
        this.repository = repository;
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

    /// Fetches a list of all users in the system.
    /// Intended for use by admins only.
    ///
    /// @param pageable the pagination options
    /// @return a paginated list of user objects
    @Transactional(readOnly = true)
    public Page<UserDTO.@NonNull UserResponseDTO> getAllUsers(Pageable pageable) {
        final var paginatedUserDTO = repository.findAll(pageable);

        log.debug("returning {} users", paginatedUserDTO.getTotalElements());

        return paginatedUserDTO.map(mapper::toDto);
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
