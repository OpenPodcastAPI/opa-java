package org.openpodcastapi.opa.feed;

import jakarta.persistence.*;
import org.openpodcastapi.opa.subscription.SubscriptionEntity;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/// An entity representing podcast feed metadata
@Entity
@Table(name = "feeds")
public class FeedEntity {
    /// The feed's database ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /// The UUIDv5 identifier for the feed.
    @Column(unique = true, nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID uuid;

    /// The URL of the subscription feed
    @Column(nullable = false)
    private String feedUrl;

    /// Linked subscriptions
    @OneToMany(mappedBy = "feed", cascade =  CascadeType.REMOVE)
    private Set<SubscriptionEntity> subscriptions;

    /// Creation timestamp
    private Instant createdAt;

    /// Last update timestamp
    private Instant updatedAt;

    /// No-args constructor
    public FeedEntity() {
    }

    /// Required-args constructor
    /// @param uuid the calculated UUIDv5 identifier for the feed
    /// @param feedUrl the URL location of the feed's XML file
    public FeedEntity(UUID uuid, String feedUrl) {
        this.uuid = uuid;
        this.feedUrl = feedUrl;
    }

    /// Updates timestamps before saving
    @PrePersist
    public void prePersist() {
        final Instant timestamp = Instant.now();
        this.setCreatedAt(timestamp);
        this.setUpdatedAt(timestamp);
    }

    /// Updates the last updated timestamp when the entity is updated
    @PreUpdate
    public void preUpdate() {
        final Instant timestamp = Instant.now();
        this.setUpdatedAt(timestamp);
    }

    /// @return the feed entity ID
    public Long getId() {
        return id;
    }

    /// @return the feed's UUIDv5 identifier
    public UUID getUuid() {
        return uuid;
    }

    /// @param uuid the calculated UUIDv5 identifier for the feed
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    /// @return the feed's URL
    public String getFeedUrl() {
        return feedUrl;
    }

    /// @param feedUrl the feed's URL
    public void setFeedUrl(String feedUrl) {
        this.feedUrl = feedUrl;
    }

    /// @return the `createdAt` timestamp for the feed entity
    public Instant getCreatedAt() {
        return createdAt;
    }

    /// @param createdAt the creation timestamp
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /// @return the `updatedAt` timestamp for the feed entity
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /// @param updatedAt the new `updatedAt` timestamp
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
