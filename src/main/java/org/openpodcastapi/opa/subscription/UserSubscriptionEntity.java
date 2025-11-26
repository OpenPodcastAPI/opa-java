package org.openpodcastapi.opa.subscription;

import jakarta.persistence.*;
import lombok.*;
import org.openpodcastapi.opa.user.UserEntity;

import java.time.Instant;
import java.util.UUID;

/// Entity representing the relationship between a user and a subscription
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "user_subscription")
public class UserSubscriptionEntity {
    /// The entity ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Generated
    private Long id;

    /// The UUID of the entity
    @Column(unique = true, nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID uuid;

    /// The associated [UserEntity]
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    /// The associated [SubscriptionEntity]
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
