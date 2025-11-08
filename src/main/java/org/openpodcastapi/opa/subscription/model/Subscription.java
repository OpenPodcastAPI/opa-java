package org.openpodcastapi.opa.subscription.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "subscriptions")
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Generated
    @Getter
    private Long id;

    @Getter
    @Setter
    @Column(unique = true, nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID uuid;

    @Getter
    @Setter
    @Column(nullable = false)
    private String feedUrl;

    @Getter
    @Setter
    @OneToMany(mappedBy = "subscription")
    private Set<UserSubscription> subscribers;

    @Getter
    @Setter
    @Column(updatable = false, nullable = false)
    private Instant createdAt;

    @Getter
    @Setter
    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
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
