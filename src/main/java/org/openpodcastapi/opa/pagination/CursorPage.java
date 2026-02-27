package org.openpodcastapi.opa.pagination;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.function.Function;

/// A generic DTO for cursor    -paginated responses
///
/// @param <T>        the type of entity
/// @param data       the list of entities to display
/// @param nextCursor the encoded cursor representing the next page of results
/// @param prevCursor the encoded cursor representing the previous page of results
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CursorPage<T>(
        List<T> data,
        String nextCursor,
        String prevCursor
) {
    /// Builder pattern for making a cursor-paginated page of results
    ///
    /// @param <T>             the type of entity to build a response for
    /// @param results         the list of results
    /// @param limit           the number of results to display
    /// @param cursorExtractor the function used to extract the cursor from the entity's `id` and `createdAt` fields
    /// @return a page of paginated results
    public static <T> CursorPage<T> of(
            List<T> results,
            int limit,
            Function<? super T, CursorPayload> cursorExtractor
    ) {
        // If there are no results, just return an empty list
        if (results.isEmpty()) {
            return new CursorPage<>(List.of(), null, null);
        }

        // Get the first and last result
        final var first = results.getFirst();
        final var last = results.getLast();

        // Initialize the cursors
        final String prevCursor = CursorUtility.encode(cursorExtractor.apply(first));
        String nextCursor = null;

        // If there is a next page, create a cursor for it
        if (results.size() == limit) {
            nextCursor = CursorUtility.encode(cursorExtractor.apply(last));
        }

        return new CursorPage<>(results, nextCursor, prevCursor);
    }

    /// Generic mapping function for mapping a given entity to a DTO inside a cursor page
    ///
    /// @param <R>    the type of DTO
    /// @param mapper the mapper instance to use
    /// @return a cursor page containing mapped entities
    public <R> CursorPage<R> map(java.util.function.Function<? super T, R> mapper) {
        List<R> mapped = data.stream().map(mapper).toList();
        return new CursorPage<>(mapped, nextCursor, prevCursor);
    }
}
