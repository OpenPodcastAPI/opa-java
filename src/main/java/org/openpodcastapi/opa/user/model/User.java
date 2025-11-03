package org.openpodcastapi.opa.user.model;

import jakarta.persistence.*;
import lombok.*;
import org.openpodcastapi.opa.subscription.model.UserSubscription;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @Getter
    @Generated
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Setter
    @Column(unique = true, nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID uuid;

    @Getter
    @Setter
    @Column(nullable = false, unique = true)
    private String username;

    @Getter
    @Setter
    @Column(nullable = false)
    private String password;

    @Getter
    @Setter
    @Column(nullable = false, unique = true)
    private String email;

    @Getter
    @Setter
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private Set<UserSubscription> subscriptions;

    @Getter
    @Setter
    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @CollectionTable(name="user_roles", joinColumns = @JoinColumn(name = "user_id"))
    private Set<UserRoles> userRoles = new HashSet<>(Collections.singletonList(UserRoles.USER));

    @Getter
    @Setter
    @Column(updatable = false)
    private Instant createdAt;

    @Getter
    @Setter
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
