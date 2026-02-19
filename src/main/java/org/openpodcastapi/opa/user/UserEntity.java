package org.openpodcastapi.opa.user;

import jakarta.persistence.*;
import org.openpodcastapi.opa.security.RefreshTokenEntity;
import org.openpodcastapi.opa.subscription.SubscriptionEntity;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/// An entity representing a user
@Entity
@Table(name = "users")
public class UserEntity {

    /// The user ID
    @Id
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

    /// A list of user subscriptions associated with the user
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private Set<SubscriptionEntity> subscriptions;

    /// A list of refresh tokens associated with the user
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private Set<RefreshTokenEntity> refreshTokens;

    /// The user's associated roles
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    private Set<UserRoles> userRoles = new HashSet<>(Collections.singletonList(UserRoles.USER));

    /// The date at which the entity was created
    @Column(updatable = false)
    private Instant createdAt;

    /// The date at which the entity was last updated
    private Instant updatedAt;

    /// No-args constructor
    public UserEntity() {
    }

    /// Required-args constructor
    ///
    /// @param id       the ID of the user
    /// @param uuid     the UUID of the user
    /// @param username the username of the user
    /// @param email    the email address of the user
    public UserEntity(Long id, UUID uuid, String username, String email) {
        this(id, uuid, username, "", email, Instant.now(), Instant.now());
    }

    /// All-args constructor
    ///
    /// @param id        the ID of the user
    /// @param uuid      the UUID of the user
    /// @param username  the user's username
    /// @param password  the user's hashed password
    /// @param email     the user's email address
    /// @param createdAt the date at which the user was created
    /// @param updatedAt the date at which the user was last updated
    public UserEntity(Long id, UUID uuid, String username, String password, String email, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.uuid = uuid;
        this.username = username;
        this.password = password;
        this.email = email;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /// Retrieves the ID of a user entity
    ///
    /// @return the ID of the user entity
    public Long getId() {
        return this.id;
    }

    /// Retrieves the UUID of a user entity
    ///
    /// @return the UUID of the user entity
    public UUID getUuid() {
        return this.uuid;
    }

    /// Sets the UUID of a user entity
    ///
    /// @param uuid the UUID for the entity
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    /// Retrieves the username of a user entity
    ///
    /// @return a user's username
    public String getUsername() {
        return this.username;
    }

    /// Sets the username of a user entity
    ///
    /// @param username the user's username
    public void setUsername(String username) {
        this.username = username;
    }

    /// Retrieves the user's password hash
    ///
    /// @return the hashed password
    public String getPassword() {
        return this.password;
    }

    /// Sets the user's password. The password must be hashed first
    ///
    /// @param password the hashed password
    public void setPassword(String password) {
        this.password = password;
    }

    /// Retrieves a user's email address
    ///
    /// @return the user's email address
    public String getEmail() {
        return this.email;
    }

    /// Sets the user's email address.
    ///
    /// @param email the user's email address
    public void setEmail(String email) {
        this.email = email;
    }

    /// Retrieves a user's subscriptions
    ///
    /// @return a set of subscriptions
    public Set<SubscriptionEntity> getSubscriptions() {
        return this.subscriptions;
    }

    /// Sets a user's subscriptions
    ///
    /// @param subscriptions the set of subscriptions to add to the user
    public void setSubscriptions(Set<SubscriptionEntity> subscriptions) {
        this.subscriptions = subscriptions;
    }

    /// Retrieves a user's roles
    ///
    /// @return a set of user roles
    public Set<UserRoles> getUserRoles() {
        return this.userRoles;
    }

    /// Sets a user's roles
    ///
    /// @param userRoles a set of user roles
    public void setUserRoles(Set<UserRoles> userRoles) {
        this.userRoles = userRoles;
    }

    /// Retrieves the creation date of the user
    ///
    /// @return the user creation date
    public Instant getCreatedAt() {
        return this.createdAt;
    }

    /// Retrieves the last updated timestamp for the user
    ///
    /// @return the last updated timestamp
    public Instant getUpdatedAt() {
        return this.updatedAt;
    }

    /// Performs actions when the entity is initially saved
    @PrePersist
    public void prePersist() {
        this.setUuid(UUID.randomUUID());
        final var timestamp = Instant.now();
        // Store the created date and set an updated timestamp
        this.createdAt = timestamp;
        this.updatedAt = timestamp;
    }

    /// Performs actions when the entity is updated
    @PreUpdate
    public void preUpdate() {
        // Store the timestamp of the update
        this.updatedAt = Instant.now();
    }
}
