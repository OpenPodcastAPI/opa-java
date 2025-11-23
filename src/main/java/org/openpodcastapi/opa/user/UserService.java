package org.openpodcastapi.opa.user;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class UserService {
    private static final String USER_NOT_FOUND = "User not found";
    private final UserRepository repository;
    private final UserMapper mapper;
    private final BCryptPasswordEncoder passwordEncoder;

    /// Persists a user to the database
    ///
    /// @param dto the [UserDTO.CreateUserDTO] for the user
    /// @return the formatted [UserDTO.UserResponseDTO] representation of the user
    /// @throws DataIntegrityViolationException if a user with a matching username or email address exists already
    @Transactional
    public UserDTO.UserResponseDTO createAndPersistUser(UserDTO.CreateUserDTO dto) throws DataIntegrityViolationException {
        // If the user already exists in the system, throw an exception and return a `400` response.
        if (repository.existsUserByEmail(dto.email()) || repository.existsUserByUsername(dto.username())) {
            throw new DataIntegrityViolationException("User already exists");
        }

        // Create a new user with a hashed password and a default `USER` role.
        UserEntity newUserEntity = mapper.toEntity(dto);
        newUserEntity.setPassword(passwordEncoder.encode(dto.password()));
        newUserEntity.getUserRoles().add(UserRoles.USER);

        // Save the user and return the DTO representation.
        UserEntity persistedUserEntity = repository.save(newUserEntity);
        log.debug("persisted user {}", persistedUserEntity.getUuid());
        return mapper.toDto(persistedUserEntity);
    }

    @Transactional(readOnly = true)
    public Page<UserDTO.UserResponseDTO> getAllUsers(Pageable pageable) {
        Page<UserEntity> users = repository.findAll(pageable);

        log.debug("returning {} users", users.getTotalElements());

        return users.map(mapper::toDto);
    }

    /// Deletes a user from the database
    ///
    /// @param uuid the [UUID] of the user to delete
    /// @return a success message
    /// @throws EntityNotFoundException if no matching record is found
    @Transactional
    public String deleteUser(UUID uuid) throws EntityNotFoundException {
        UserEntity userEntity = repository.getUserByUuid(uuid).orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));

        repository.delete(userEntity);

        return "user " + uuid.toString() + "deleted";
    }
}
