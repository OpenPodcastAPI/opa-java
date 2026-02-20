package org.openpodcastapi.opa.subscription;

import jakarta.persistence.*;
import org.openpodcastapi.opa.feed.FeedEntity;
import org.openpodcastapi.opa.user.UserEntity;

import java.time.Instant;
import java.util.UUID;

/// Entity representing the relationship between a user and a subscription
@Entity
@Table(name = "subscriptions")
public class SubscriptionEntity {
    /// The entity ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /// The UUID of the entity
    @Column(unique = true, nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID uuid;

    /// The associated user
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    /// The associated subscription
    @ManyToOne
    @JoinColumn(name = "feed_id")
    private FeedEntity feed;

    /// The date at which the user subscription was created
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /// The date at which the user subscription was last updated
    @Column(nullable = false)
    private Instant updatedAt;

    /// The timestamp representing when the user unsubscribed from the feed
    @Column
    private Instant unsubscribedAt;

    /// No-args constructor
    public SubscriptionEntity() {
    }

    /// Required-args constructor
    ///
    /// @param uuid the UUID of the entity
    /// @param user the user associated with the user subscription
    /// @param feed the feed associated with the user subscription
    public SubscriptionEntity(UUID uuid, UserEntity user, FeedEntity feed) {
        this.uuid = uuid;
        this.user = user;
        this.feed = feed;
    }

    /// @return the subscription ID
    public Long getId() {
        return this.id;
    }

    /// @param id the subscription ID
    public void setId(Long id) {
        this.id = id;
    }

    /// @return the subscription UUID
    public UUID getUuid() {
        return this.uuid;
    }

    /// @param uuid the subscription UUID
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    /// @return the associated user
    public UserEntity getUser() {
        return this.user;
    }

    /// @param user the subscription UUID
    public void setUser(UserEntity user) {
        this.user = user;
    }

    /// @return the associated subscription
    public FeedEntity getFeed() {
        return this.feed;
    }

    /// @param feed the subscription UUID
    public void setFeed(FeedEntity feed) {
        this.feed = feed;
    }

    /// @return the creation date
    public Instant getCreatedAt() {
        return this.createdAt;
    }

    /// @param createdAt the creation timestamp
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /// @return the last updated timestamp
    public Instant getUpdatedAt() {
        return this.updatedAt;
    }

    /// @param updatedAt the last updated timestamp
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    /// @return the unsubscribed timestamp
    public Instant getUnsubscribedAt() {
        return this.unsubscribedAt;
    }

    /// @param unsubscribedAt the unsubscribed timestamp
    public void setUnsubscribedAt(Instant unsubscribedAt) {
        this.unsubscribedAt = unsubscribedAt;
    }

    /// Performs actions on initial save
    @PrePersist
    public void prePersist() {
        this.setUuid(UUID.randomUUID());
        final Instant timestamp = Instant.now();
        // Store the created date and set an updated timestamp
        this.setCreatedAt(timestamp);
        this.setUpdatedAt(timestamp);
    }

    /// Performs actions when an entity is updated
    @PreUpdate
    public void preUpdate() {
        // Store the timestamp of the update
        this.setUpdatedAt(Instant.now());
    }
}
