package org.openpodcastapi.opa.security;

import jakarta.persistence.*;
import lombok.*;
import org.openpodcastapi.opa.user.UserEntity;

import java.time.Instant;

/// Entity for refresh tokens
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "refresh_tokens")
public class RefreshTokenEntity {
    /// The token ID
    @Id
    @Generated
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /// The hashed representation of the token
    @Column(nullable = false, unique = true)
    private String tokenHash;

    /// The user that owns the token
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private UserEntity user;

    /// The date at which the token expires
    @Column(nullable = false)
    private Instant expiresAt;

    /// The date at which the token was created
    @Column(nullable = false)
    private Instant createdAt;

    /// Performs actions on initial save
    @PrePersist
    public void prePersist() {
        this.setCreatedAt(Instant.now());
    }
}

