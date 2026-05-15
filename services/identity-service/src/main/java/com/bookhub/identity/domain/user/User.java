package com.bookhub.identity.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
    name = "users",
    uniqueConstraints = {
      @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
      @UniqueConstraint(name = "uk_users_username", columnNames = "username")
    })
public class User {

  @Id private UUID id;

  @Column(name = "username", nullable = false, length = 50)
  private String username;

  @Column(name = "email", nullable = false, length = 255)
  private String email;

  @Column(name = "password_hash", nullable = false, length = 100)
  private String passwordHash;

  @Column(name = "display_name", nullable = false, length = 100)
  private String displayName;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false, length = 20)
  private UserRole role;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected User() {}

  private User(
      final UUID id,
      final String username,
      final String email,
      final String passwordHash,
      final String displayName,
      final UserRole role,
      final Instant createdAt,
      final Instant updatedAt) {
    this.id = id;
    this.username = username;
    this.email = email;
    this.passwordHash = passwordHash;
    this.displayName = displayName;
    this.role = role;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static User create(
      final String username,
      final String email,
      final String passwordHash,
      final String displayName,
      final UserRole role) {
    return new User(null, username, email, passwordHash, displayName, role, null, null);
  }

  public static User rehydrate(
      final UUID id,
      final String username,
      final String email,
      final String passwordHash,
      final String displayName,
      final UserRole role) {
    return new User(id, username, email, passwordHash, displayName, role, null, null);
  }

  @PrePersist
  public void prePersist() {
    if (id == null) {
      id = UUID.randomUUID();
    }
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

  public void updatePassword(final String encodedPassword) {
    this.passwordHash = encodedPassword;
    this.updatedAt = Instant.now();
  }

  public void changeRole(final UserRole newRole) {
    this.role = newRole;
    this.updatedAt = Instant.now();
  }

  @Override
  public boolean equals(final Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof User user)) {
      return false;
    }
    return id != null && Objects.equals(id, user.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  public UUID getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getEmail() {
    return email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public String getDisplayName() {
    return displayName;
  }

  public UserRole getRole() {
    return role;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
