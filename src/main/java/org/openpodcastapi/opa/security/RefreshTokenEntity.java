package org.openpodcastapi.opa.security;

import jakarta.persistence.*;
import org.openpodcastapi.opa.user.UserEntity;

import java.time.Instant;

/// Entity for refresh tokens
@Entity
@Table(name = "refresh_tokens")
public class RefreshTokenEntity {
    /// The token ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /// The hashed representation of the token
    @Column(nullable = false, unique = true)
    private String tokenHash;

    /// The user that owns the token
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    /// The date at which the token expires
    @Column(nullable = false)
    private Instant expiresAt;

    /// The date at which the token was created
    @Column(nullable = false)
    private Instant createdAt;

    /// No-args constructor
    public RefreshTokenEntity() {
    }

    /// Required-args constructor
    ///
    /// @param tokenHash the hash of the token
    /// @param user      the user associated with the token
    /// @param expiresAt the expiry date of the token
    public RefreshTokenEntity(String tokenHash, UserEntity user, Instant expiresAt) {
        this.tokenHash = tokenHash;
        this.user = user;
        this.expiresAt = expiresAt;
    }

    /// Retrieves the ID of the refresh token entity
    ///
    /// @return the ID of the entity
    public Long getId() {
        return id;
    }

    /// Retrieves the token hash for a token
    ///
    /// @return the token hash
    public String getTokenHash() {
        return tokenHash;
    }

    /// Retrieves the user associated with a refresh token
    ///
    /// @return the user associated with the token
    public UserEntity getUser() {
        return user;
    }

    /// Assigns a user to a refresh token
    ///
    /// @param user the user associated with the token
    public void setUser(UserEntity user) {
        this.user = user;
    }

    /// Returns the expiry date of a token
    ///
    /// @return the expiry date for the token
    public Instant getExpiresAt() {
        return expiresAt;
    }

    /// Sets the expiry date for a token
    ///
    /// @param expiresAt the expiry date of the token
    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    /// Retrieves the creation date for a token
    ///
    /// @return the creation date of the token
    public Instant getCreatedAt() {
        return createdAt;
    }

    /// Sets the created date for a token
    ///
    /// @param createdAt the created date of the token
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /// Performs actions on initial save
    @PrePersist
    public void prePersist() {
        this.setCreatedAt(Instant.now());
    }
}

