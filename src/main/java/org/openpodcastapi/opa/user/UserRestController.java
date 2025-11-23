package org.openpodcastapi.opa.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserRestController {
    private final UserService service;

    /// Returns all users
    ///
    /// @param pageable the [Pageable] options used for pagination
    /// @return a [ResponseEntity] containing [UserDTO.UserPageDTO] objects
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO.UserPageDTO> getAllUsers(Pageable pageable) {
        Page<UserDTO.UserResponseDTO> users = service.getAllUsers(pageable);

        return new ResponseEntity<>(UserDTO.UserPageDTO.fromPage(users), HttpStatus.OK);
    }

    /// Creates a new user in the system
    ///
    /// @param request a [UserDTO.CreateUserDTO] request body
    /// @return a [ResponseEntity] containing [UserDTO.UserResponseDTO] objects
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UserDTO.UserResponseDTO> createUser(@RequestBody @Validated UserDTO.CreateUserDTO request) {
        // Create and persist the user
        UserDTO.UserResponseDTO dto = service.createAndPersistUser(request);

        // Return the user DTO with a `201` status.
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    /// Fetch a specific user by UUID
    ///
    /// @param uuid the [UUID] of the user
    /// @return a [ResponseEntity] containing a summary of the action
    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN') or #uuid == principal.uuid")
    public ResponseEntity<String> deleteUser(@PathVariable String uuid) {
        // Attempt to validate the UUID value from the provided string
        // If the value is invalid, the GlobalExceptionHandler will throw a 400.
        UUID uuidValue = UUID.fromString(uuid);

        // Delete the user and return the status string
        String status = service.deleteUser(uuidValue);

        return new ResponseEntity<>(status, HttpStatus.ACCEPTED);
    }
}
