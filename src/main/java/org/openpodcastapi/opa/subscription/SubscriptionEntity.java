package org.openpodcastapi.opa.subscription;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/// An entity representing a subscription wrapper
@Entity
@Table(name = "subscriptions")
public class SubscriptionEntity {
    /// The subscription ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /// The UUID of the subscription.
    /// This should be calculated by the client based on the feed URL
    @Column(unique = true, nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID uuid;

    /// The URL of the subscription feed
    @Column(nullable = false)
    private String feedUrl;

    /// A list of user subscriptions associated with the subscription
    @OneToMany(mappedBy = "subscription", cascade = CascadeType.REMOVE)
    private Set<UserSubscriptionEntity> subscribers;

    /// The date at which the subscription was created
    @Column(updatable = false, nullable = false)
    private Instant createdAt;

    /// The date at which the subscription was last updated
    @Column(nullable = false)
    private Instant updatedAt;

    /// No-args constructor
    public SubscriptionEntity() {
    }

    /// Required-args constructor
    ///
    /// @param uuid    the UUID of the subscription
    /// @param feedUrl the feed URL of the subscription
    public SubscriptionEntity(UUID uuid, String feedUrl) {
        this.uuid = uuid;
        this.feedUrl = feedUrl;
    }

    /// Performs actions when the entity is initially saved
    @PrePersist
    public void prePersist() {
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

    /// Retrieves the ID of a subscription
    ///
    /// @return the ID of the subscription
    public Long getId() {
        return this.id;
    }

    /// Retrieves the UUID of a subscription
    ///
    /// @return the UUID of the subscription
    public UUID getUuid() {
        return this.uuid;
    }

    /// Sets the UUID of a subscription
    ///
    /// @param uuid the UUID of the subscription
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    /// Retrieves the feed URL of a subscription
    ///
    /// @return the feed URL associated with a subscription
    public String getFeedUrl() {
        return this.feedUrl;
    }

    /// Sets the feed URL of a subscription
    ///
    /// @param feedUrl the feed URL
    public void setFeedUrl(String feedUrl) {
        this.feedUrl = feedUrl;
    }

    /// Retrieves the creation date of a subscription
    ///
    /// @return the creation date of the subscription
    public Instant getCreatedAt() {
        return this.createdAt;
    }

    /// Sets the creation date of a subscription
    ///
    /// @param createdAt the creation date of the subscription
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /// Retrieves the last update time of a subscription
    ///
    /// @return the date at which the subscription was last updated
    public Instant getUpdatedAt() {
        return this.updatedAt;
    }

    /// Sets the last update time of a subscription
    ///
    /// @param updatedAt the date at which the subscription was last updated
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
