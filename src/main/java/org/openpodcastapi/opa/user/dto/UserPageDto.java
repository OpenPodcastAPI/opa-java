package org.openpodcastapi.opa.user.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/// A paginated DTO representing a list of subscriptions
///
/// @param users            the [UserDto] list representing the users
/// @param first            whether this is the first page
/// @param last             whether this is the last page
/// @param page             the current page number
/// @param totalPages       the total number of pages in the result set
/// @param numberOfElements the number of elements in the current page
/// @param totalElements    the total number of elements in the result set
/// @param size             the size limit applied to the page
public record UserPageDto(
        List<UserDto> users,
        boolean first,
        boolean last,
        int page,
        int totalPages,
        long totalElements,
        int numberOfElements,
        int size
) {
    public static UserPageDto fromPage(Page<UserDto> page) {
        return new UserPageDto(
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
