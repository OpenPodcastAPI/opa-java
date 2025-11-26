package org.openpodcastapi.opa.user;

import jakarta.persistence.*;
import lombok.*;
import org.openpodcastapi.opa.subscription.UserSubscriptionEntity;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/// An entity representing a user
@Entity
@Table(name = "users")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity implements Serializable {

    /// The user ID
    @Id
    @Generated
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /// The user UUID
    @Column(unique = true, nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID uuid;

    /// The user's username
    @Column(nullable = false, unique = true)
    private String username;

    /// The user's hashed password
    @Column(nullable = false)
    private String password;

    /// The user's email address
    @Column(nullable = false, unique = true)
    private String email;

    /// A list of [UserSubscriptionEntity] associated with the user
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private transient Set<UserSubscriptionEntity> subscriptions;

    /// The user's associated roles
    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    private Set<UserRoles> userRoles = new HashSet<>(Collections.singletonList(UserRoles.USER));

    /// The date at which the entity was created
    @Column(updatable = false)
    private Instant createdAt;

    /// The date at which the entity was last updated
    private Instant updatedAt;

    /// Performs actions when the entity is initially saved
    @PrePersist
    public void prePersist() {
        this.setUuid(UUID.randomUUID());
        final Instant timestamp = Instant.now();
        // Store the created date and set an updated timestamp
        this.setCreatedAt(timestamp);
        this.setUpdatedAt(timestamp);
    }

    /// Performs actions when the entity is updated
    @PreUpdate
    public void preUpdate() {
        // Store the timestamp of the update
        this.setUpdatedAt(Instant.now());
    }
}
