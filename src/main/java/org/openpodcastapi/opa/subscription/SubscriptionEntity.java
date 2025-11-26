package org.openpodcastapi.opa.subscription;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/// An entity representing a subscription wrapper
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "subscriptions")
public class SubscriptionEntity {
    /// The subscription ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Generated
    private Long id;

    /// The UUID of the subscription.
    /// This should be calculated by the client based on the feed URL
    @Column(unique = true, nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID uuid;

    /// The URL of the subscription feed
    @Column(nullable = false)
    private String feedUrl;

    /// A list of [UserSubscriptionEntity] associated with the subscription
    @OneToMany(mappedBy = "subscription")
    private Set<UserSubscriptionEntity> subscribers;

    /// The date at which the subscription was created
    @Column(updatable = false, nullable = false)
    private Instant createdAt;

    /// The date at which the subscription was last updated
    @Column(nullable = false)
    private Instant updatedAt;

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
}
