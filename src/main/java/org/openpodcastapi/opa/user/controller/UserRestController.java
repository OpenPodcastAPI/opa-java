package org.openpodcastapi.opa.user.controller;

import lombok.RequiredArgsConstructor;
import org.openpodcastapi.opa.user.dto.CreateUserDto;
import org.openpodcastapi.opa.user.dto.UserDto;
import org.openpodcastapi.opa.user.dto.UserPageDto;
import org.openpodcastapi.opa.user.service.UserService;
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

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<UserPageDto> getAllUsers(Pageable pageable) {
        Page<UserDto> users = service.getAllUsers(pageable);

        return new ResponseEntity<>(UserPageDto.fromPage(users), HttpStatus.OK);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UserDto> createUser(@RequestBody @Validated CreateUserDto request) {
        // Create and persist the user
        UserDto dto = service.createAndPersistUser(request);

        // Return the user DTO with a `201` status.
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

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
