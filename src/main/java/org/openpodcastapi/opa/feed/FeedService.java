package org.openpodcastapi.opa.feed;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static org.slf4j.LoggerFactory.getLogger;

/// Service for feed operations
@Service
public class FeedService {
    private static final Logger log = getLogger(FeedService.class);
    private final FeedRepository repository;
    private final FeedMapper mapper;

    /// All-args constructor
    ///
    /// @param repository the [FeedRepository] for database interactions
    /// @param mapper     the [FeedMapper] for DTO mapping
    public FeedService(FeedRepository repository, FeedMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /// Fetches an existing repository from the database or creates a new one if none is found
    ///
    /// @param dto the DTO containing the subscription data
    /// @return the fetched or created subscription
    public FeedEntity fetchOrCreateFeed(FeedDTO.NewFeedRequestDTO dto) {
        final var feedUuid = UUID.fromString(dto.uuid());

        log.debug("Searching for existing feed with UUID {}", feedUuid);

        return repository
                .findByUuid(feedUuid)
                .orElseGet(() -> {
                    log.info("Creating new subscription with UUID {} and feed URL {}", dto.uuid(), dto.feedUrl());
                    return repository.save(mapper.toEntity(dto));
                });
    }
}
