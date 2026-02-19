package org.openpodcastapi.opa.feed;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/// Helper class to map feed entities and DTOs
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FeedMapper {
    /// Maps an incoming feed creation request to an entity
    ///
    /// @param dto the DTO to map
    /// @return a mapped [FeedEntity]
    FeedEntity toEntity(FeedDTO.NewFeedRequestDTO dto);
}
