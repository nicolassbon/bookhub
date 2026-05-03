package com.bookhub.library.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.Builder;

public class Notification {

  private UUID id;
  private final UUID userId;
  private final NotificationType type;
  private final String title;
  private final String message;
  private final String payload; // JSON string representation
  private NotificationStatus status;
  private final Instant createdAt;
  private Instant readAt;

  private Notification(
      final UUID id,
      final UUID userId,
      final NotificationType type,
      final String title,
      final String message,
      final String payload,
      final NotificationStatus status,
      final Instant createdAt,
      final Instant readAt) {
    this.id = id;
    this.userId = userId;
    this.type = type;
    this.title = title;
    this.message = message;
    this.payload = payload;
    this.status = status;
    this.createdAt = createdAt;
    this.readAt = readAt;
  }

  public static Notification create(
      final UUID userId,
      final NotificationType type,
      final String title,
      final String message,
      final String payload) {
    Objects.requireNonNull(userId, "userId must not be null");
    Objects.requireNonNull(type, "type must not be null");
    Objects.requireNonNull(title, "title must not be null");
    Objects.requireNonNull(message, "message must not be null");

    return new Notification(
        null,
        userId,
        type,
        title,
        message,
        payload,
        NotificationStatus.UNREAD,
        Instant.now(),
        null);
  }

  @Builder(builderMethodName = "rehydrateBuilder")
  public static Notification rehydrate(
      final UUID id,
      final UUID userId,
      final NotificationType type,
      final String title,
      final String message,
      final String payload,
      final NotificationStatus status,
      final Instant createdAt,
      final Instant readAt) {
    return new Notification(id, userId, type, title, message, payload, status, createdAt, readAt);
  }

  public void markAsRead() {
    if (this.status == NotificationStatus.UNREAD) {
      this.status = NotificationStatus.READ;
      this.readAt = Instant.now();
    }
  }

  public boolean isOwnedBy(final UUID userId) {
    return this.userId.equals(userId);
  }

  public UUID getId() {
    return id;
  }

  public void setId(final UUID id) {
    this.id = id;
  }

  public UUID getUserId() {
    return userId;
  }

  public NotificationType getType() {
    return type;
  }

  public String getTitle() {
    return title;
  }

  public String getMessage() {
    return message;
  }

  public String getPayload() {
    return payload;
  }

  public NotificationStatus getStatus() {
    return status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getReadAt() {
    return readAt;
  }

  @Override
  public boolean equals(final Object other) {
    if (this == other) return true;
    if (!(other instanceof Notification that)) return false;
    return id != null && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
