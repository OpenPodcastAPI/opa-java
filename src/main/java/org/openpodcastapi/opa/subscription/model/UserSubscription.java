package org.openpodcastapi.opa.subscription.model;

import jakarta.persistence.*;
import lombok.*;
import org.openpodcastapi.opa.user.model.User;

import java.time.Instant;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user_subscription")
public class UserSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Generated
    private Long id;

    @Getter
    @Setter
    @Column(unique = true, nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID uuid;

    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    @Getter
    @Setter
    @Column(columnDefinition = "boolean default true")
    private Boolean isSubscribed;

    @Getter
    @Setter
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Getter
    @Setter
    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        this.setUuid(UUID.randomUUID());
        final Instant timestamp = Instant.now();
        // Store the created date and set an updated timestamp
        this.setCreatedAt(timestamp);
        this.setUpdatedAt(timestamp);
    }

    @PreUpdate
    public void preUpdate() {
        // Store the timestamp of the update
        this.setUpdatedAt(Instant.now());
    }
}
