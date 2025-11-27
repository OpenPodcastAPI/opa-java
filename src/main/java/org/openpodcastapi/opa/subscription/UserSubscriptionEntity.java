package org.openpodcastapi.opa.subscription;

import jakarta.persistence.*;
import org.openpodcastapi.opa.user.UserEntity;

import java.time.Instant;
import java.util.UUID;

/// Entity representing the relationship between a user and a subscription
@Entity
@Table(name = "user_subscription")
public class UserSubscriptionEntity {
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
    @JoinColumn(name = "subscription_id")
    private SubscriptionEntity subscription;

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
    public UserSubscriptionEntity() {
    }

    /// Required-args constructor
    ///
    /// @param uuid         the UUID of the entity
    /// @param user         the user associated with the user subscription
    /// @param subscription the subscription associated with the user subscription
    public UserSubscriptionEntity(UUID uuid, UserEntity user, SubscriptionEntity subscription) {
        this.uuid = uuid;
        this.user = user;
        this.subscription = subscription;
    }

    /// Retrieves the subscription ID
    ///
    /// @return the subscription ID
    public Long getId() {
        return this.id;
    }

    /// Sets the subscription ID
    ///
    /// @param id the subscription ID
    public void setId(Long id) {
        this.id = id;
    }

    /// Retrieves the subscription UUID
    ///
    /// @return the subscription UUID
    public UUID getUuid() {
        return this.uuid;
    }

    /// Sets the subscription UUID
    ///
    /// @param uuid the subscription UUID
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    /// Retrieves the user associated with the entity
    ///
    /// @return the associated user
    public UserEntity getUser() {
        return this.user;
    }

    /// Sets the user associated with the subscription
    ///
    /// @param user the subscription UUID
    public void setUser(UserEntity user) {
        this.user = user;
    }

    /// Retrieves the subscription associated with the entity
    ///
    /// @return the associated subscription
    public SubscriptionEntity getSubscription() {
        return this.subscription;
    }

    /// Sets the subscription associated with the user subscription
    ///
    /// @param subscription the subscription UUID
    public void setSubscription(SubscriptionEntity subscription) {
        this.subscription = subscription;
    }

    /// Retrieves the creation date of the subscription
    ///
    /// @return the creation date
    public Instant getCreatedAt() {
        return this.createdAt;
    }

    /// Sets the creation date for the subscription
    ///
    /// @param createdAt the creation timestamp
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /// Retrieves the last update timestamp for the subscription
    ///
    /// @return the last updated timestamp
    public Instant getUpdatedAt() {
        return this.updatedAt;
    }

    /// Sets the last updated date for the subscription
    ///
    /// @param updatedAt the last updated timestamp
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    /// Retrieves the unsubscribed timestamp for the entity
    ///
    /// @return the unsubscribed timestamp
    public Instant getUnsubscribedAt() {
        return this.unsubscribedAt;
    }

    /// Sets the unsubscribed timestamp
    ///
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
