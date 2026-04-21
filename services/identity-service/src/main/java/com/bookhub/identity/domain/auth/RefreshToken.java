package com.bookhub.identity.domain.auth;

import com.bookhub.identity.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @Column(name = "token_hash", nullable = false, updatable = false, length = 64)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked", nullable = false)
    private boolean revoked;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected RefreshToken() {
    }

    private RefreshToken(
            final String tokenHash,
            final User user,
            final Instant expiresAt,
            final boolean revoked,
            final Instant revokedAt,
            final Instant createdAt,
            final Instant updatedAt) {
        this.tokenHash = tokenHash;
        this.user = user;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
        this.revokedAt = revokedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static RefreshToken issue(
            final String tokenHash,
            final User user,
            final Instant expiresAt) {
        return new RefreshToken(tokenHash, user, expiresAt, false, null, null, null);
    }

    public static RefreshToken rehydrate(
            final String tokenHash,
            final User user,
            final Instant expiresAt,
            final boolean revoked,
            final Instant revokedAt) {
        return new RefreshToken(tokenHash, user, expiresAt, revoked, revokedAt, null, null);
    }

    @PrePersist
    public void prePersist() {
        final Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    public void revoke(final Instant now) {
        if (revoked) {
            return;
        }
        revoked = true;
        revokedAt = now;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public User getUser() {
        return user;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isRevoked() {
        return revoked;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RefreshToken refreshToken)) {
            return false;
        }
        return tokenHash != null && Objects.equals(tokenHash, refreshToken.tokenHash);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(tokenHash);
    }
}
