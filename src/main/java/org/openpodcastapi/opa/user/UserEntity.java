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

@Entity
@Table(name = "users")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity implements Serializable {

    @Id
    @Generated
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID uuid;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private transient Set<UserSubscriptionEntity> subscriptions;

    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    private Set<UserRoles> userRoles = new HashSet<>(Collections.singletonList(UserRoles.USER));

    @Column(updatable = false)
    private Instant createdAt;

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
