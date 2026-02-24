package org.openpodcastapi.opa.pagination;

import java.time.Instant;

/// Generic shape of pagination pagination,
///
/// @param createdAt the `created_at` timestamp of the entity
/// @param id        the database `id` of the entity
public record CursorPayload(Instant createdAt, Long id) {
}
