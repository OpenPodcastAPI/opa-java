package org.openpodcastapi.opa.user.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.openpodcastapi.opa.user.dto.CreateUserDto;
import org.openpodcastapi.opa.user.dto.UserDto;
import org.openpodcastapi.opa.user.mapper.UserMapper;
import org.openpodcastapi.opa.user.model.User;
import org.openpodcastapi.opa.user.model.UserRoles;
import org.openpodcastapi.opa.user.repository.UserRepository;
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
    /// @param dto the [CreateUserDto] for the user
    /// @return the formatted DTO representation of the user
    /// @throws DataIntegrityViolationException if a user with a matching username or email address exists already
    @Transactional
    public UserDto createAndPersistUser(CreateUserDto dto) throws DataIntegrityViolationException {
        // If the user already exists in the system, throw an exception and return a `400` response.
        if (repository.existsUserByEmail(dto.email()) || repository.existsUserByUsername(dto.username())) {
            throw new DataIntegrityViolationException("User already exists");
        }

        // Create a new user with a hashed password and a default `USER` role.
        User newUser = mapper.toEntity(dto);
        newUser.setPassword(passwordEncoder.encode(dto.password()));
        newUser.getUserRoles().add(UserRoles.USER);

        // Save the user and return the DTO representation.
        User persistedUser = repository.save(newUser);
        log.debug("persisted user {}", persistedUser.getUuid());
        return mapper.toDto(persistedUser);
    }

    /// Fetches a user record by UUID and returns a mapped DTO.
    ///
    /// @param uuid the UUID of the user to fetch
    /// @return the formatted DTO representation of the user
    /// @throws EntityNotFoundException if no matching record is found
    @Transactional(readOnly = true)
    public UserDto getUser(UUID uuid) throws EntityNotFoundException {
        // Attempt to fetch the user from the database.
        User user = repository.getUserByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));

        log.debug("user {} found", user.getUuid());
        return mapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsers(Pageable pageable) {
        Page<User> users = repository.findAll(pageable);

        log.debug("returning {} users", users.getTotalElements());

        return users.map(mapper::toDto);
    }

    /// Promotes a user to admin.
    ///
    /// @param uuid the UUID of the user to be promoted
    /// @throws EntityNotFoundException if no matching record is found
    @Transactional
    public void promoteUserToAdmin(UUID uuid) {
        // Attempt to fetch the user from the database.
        // If the user doesn't exist, throw a not found exception and return a `404` response.
        User user = repository.getUserByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));

        // Add the `ADMIN` role to the user and persist it in the database.
        user.getUserRoles().add(UserRoles.ADMIN);
        log.debug("admin role added to user {}", user.getUuid());
        repository.save(user);
    }

    /// Demotes a user by removing the ADMIN role.
    ///
    /// @param uuid the UUID of the user to demote.
    /// @throws EntityNotFoundException if no matching record is found
    @Transactional
    public void demoteUser(UUID uuid) {
        // Attempt to fetch the user from the database.
        // If the user doesn't exist, throw a not found exception and return a `404` response.
        User user = repository.getUserByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));

        // Remove the `ADMIN` role from the user and persist it in the database.
        user.getUserRoles().remove(UserRoles.ADMIN);
        log.debug("admin role removed from user {}", user.getUuid());
        repository.save(user);
    }

    /// Deletes a user from the database
    ///
    /// @param uuid the UUID of the user to delete
    /// @return a success message
    /// @throws EntityNotFoundException if no matching record is found
    @Transactional
    public String deleteUser(UUID uuid) throws EntityNotFoundException {
        User user = repository.getUserByUuid(uuid).orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));

        repository.delete(user);

        return "user " + uuid.toString() + "deleted";
    }
}
