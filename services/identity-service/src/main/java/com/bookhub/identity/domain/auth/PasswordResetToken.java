package com.bookhub.identity.domain.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
    name = "password_reset_tokens",
    uniqueConstraints = {
      @UniqueConstraint(name = "uk_password_reset_tokens_token_hash", columnNames = "token_hash"),
      @UniqueConstraint(name = "uk_password_reset_tokens_user_id", columnNames = "user_id")
    })
public class PasswordResetToken {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "token_hash", nullable = false, length = 64)
  private String tokenHash;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected PasswordResetToken() {}

  private PasswordResetToken(
      final UUID id,
      final String tokenHash,
      final UUID userId,
      final Instant expiresAt,
      final Instant createdAt) {
    this.id = id;
    this.tokenHash = tokenHash;
    this.userId = userId;
    this.expiresAt = expiresAt;
    this.createdAt = createdAt;
  }

  public static PasswordResetToken issue(
      final String tokenHash, final UUID userId, final Instant expiresAt) {
    return new PasswordResetToken(null, tokenHash, userId, expiresAt, null);
  }

  public static PasswordResetToken rehydrate(
      final UUID id, final String tokenHash, final UUID userId, final Instant expiresAt) {
    return new PasswordResetToken(id, tokenHash, userId, expiresAt, null);
  }

  @PrePersist
  public void prePersist() {
    if (id == null) {
      id = UUID.randomUUID();
    }
    if (createdAt == null) {
      createdAt = Instant.now();
    }
  }

  public UUID getId() {
    return id;
  }

  public String getTokenHash() {
    return tokenHash;
  }

  public UUID getUserId() {
    return userId;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  @Override
  public boolean equals(final Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof PasswordResetToken that)) {
      return false;
    }
    return id != null && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
