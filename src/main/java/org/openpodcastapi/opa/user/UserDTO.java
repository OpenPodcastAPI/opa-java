package org.openpodcastapi.opa.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/// Container for all user-related data transfer objects
public class UserDTO {
    /// A DTO representing a user response over the api
    ///
    /// @param uuid      the UUID of the user
    /// @param username  the username of the user
    /// @param email     the email address of the user
    /// @param createdAt the timestamp at which the user was created
    /// @param updatedAt the timestamp at which the user was last updated
    public record UserResponseDTO(
            @JsonProperty(required = true) UUID uuid,
            @JsonProperty(required = true) String username,
            @JsonProperty(required = true) String email,
            @JsonProperty(required = true) Instant createdAt,
            @JsonProperty(required = true) Instant updatedAt
    ) {
    }

    /// A paginated DTO representing a list of subscriptions
    ///
    /// @param users            the DTO list representing the users
    /// @param first            whether this is the first page
    /// @param last             whether this is the last page
    /// @param page             the current page number
    /// @param totalPages       the total number of pages in the result set
    /// @param numberOfElements the number of elements in the current page
    /// @param totalElements    the total number of elements in the result set
    /// @param size             the size limit applied to the page
    public record UserPageDTO(
            List<UserResponseDTO> users,
            boolean first,
            boolean last,
            int page,
            int totalPages,
            long totalElements,
            int numberOfElements,
            int size
    ) {
        /// Returns a paginated response with details from a paginated list of users
        ///
        /// @param page a paginated list of user DTOs
        /// @return a DTO with pagination details filled out
        public static UserPageDTO fromPage(Page<@NonNull UserResponseDTO> page) {
            return new UserPageDTO(
                    page.getContent(),
                    page.isFirst(),
                    page.isLast(),
                    page.getNumber(),
                    page.getTotalPages(),
                    page.getTotalElements(),
                    page.getNumberOfElements(),
                    page.getSize()
            );
        }
    }

    /// A DTO representing a new user
    ///
    /// @param email    the user's email address
    /// @param username the user's username
    /// @param password the user's unhashed password
    public record CreateUserDTO(
            @JsonProperty(required = true) @NotNull String username,
            @JsonProperty(required = true) @NotNull String password,
            @JsonProperty(required = true) @NotNull @Email String email
    ) {
    }
}
