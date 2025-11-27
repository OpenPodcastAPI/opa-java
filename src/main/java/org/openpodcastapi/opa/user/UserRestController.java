package org.openpodcastapi.opa.user;

import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/// Controller for user-related API requests
@RestController
@RequestMapping("/api/v1/users")
public class UserRestController {
    private final UserService service;

    /// Required-args constructor
    ///
    /// @param userService the user service used to handle user interactions
    public UserRestController(UserService userService) {
        this.service = userService;
    }

    /// Returns all users. Only accessible to admins.
    ///
    /// @param pageable the pagination options
    /// @return a response containing user objects
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO.@NonNull UserPageDTO> getAllUsers(Pageable pageable) {
        final var paginatedUserResponse = service.getAllUsers(pageable);

        return new ResponseEntity<>(UserDTO.UserPageDTO.fromPage(paginatedUserResponse), HttpStatus.OK);
    }

    /// Creates a new user in the system
    ///
    /// @param request a user creation request body
    /// @return a response containing user objects
    @PostMapping
    public ResponseEntity<UserDTO.@NonNull UserResponseDTO> createUser(@RequestBody @Validated UserDTO.CreateUserDTO request) {
        // Create and persist the user
        final var userResponseDTO = service.createAndPersistUser(request);

        // Return the user DTO with a `201` status.
        return new ResponseEntity<>(userResponseDTO, HttpStatus.CREATED);
    }

    /// Fetch a specific user by UUID
    ///
    /// @param uuid the UUID of the user
    /// @return a response containing a summary of the action
    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN') or #uuid == principal.uuid")
    public ResponseEntity<@NonNull String> deleteUser(@PathVariable String uuid) {
        // Attempt to validate the UUID value from the provided string
        // If the value is invalid, the GlobalExceptionHandler will throw a 400.
        final var uuidValue = UUID.fromString(uuid);

        // Delete the user and return the message string
        final var message = service.deleteUserAndReturnMessage(uuidValue);

        return new ResponseEntity<>(message, HttpStatus.ACCEPTED);
    }
}
