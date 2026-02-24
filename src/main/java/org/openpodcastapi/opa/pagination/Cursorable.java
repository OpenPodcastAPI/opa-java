package org.openpodcastapi.opa.pagination;

import java.time.Instant;

/// An interface for results that can be paginated using a pagination.
/// Any cursorable entity must have methods for fetching the database ID and created timestamp.
public interface Cursorable {
    Long getId();
    Instant getCreatedAt();
}
