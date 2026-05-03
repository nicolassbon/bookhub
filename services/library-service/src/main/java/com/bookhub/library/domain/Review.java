package com.bookhub.library.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.Builder;

public class Review {

  private UUID id;
  private final UUID userId;
  private final UUID bookId;
  private int rating;
  private String content;
  private ReviewStatus status;
  private final Instant createdAt;
  private Instant updatedAt;

  private Review(
      final UUID id,
      final UUID userId,
      final UUID bookId,
      final int rating,
      final String content,
      final ReviewStatus status,
      final Instant createdAt,
      final Instant updatedAt) {
    this.id = id;
    this.userId = userId;
    this.bookId = bookId;
    this.rating = rating;
    this.content = content;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static Review create(
      final UUID userId, final UUID bookId, final int rating, final String content) {
    Objects.requireNonNull(userId, "userId must not be null");
    Objects.requireNonNull(bookId, "bookId must not be null");
    validateRating(rating);

    final Instant now = Instant.now();
    return new Review(null, userId, bookId, rating, content, ReviewStatus.VISIBLE, now, now);
  }

  @Builder(builderMethodName = "rehydrateBuilder")
  public static Review rehydrate(
      final UUID id,
      final UUID userId,
      final UUID bookId,
      final int rating,
      final String content,
      final ReviewStatus status,
      final Instant createdAt,
      final Instant updatedAt) {
    return new Review(id, userId, bookId, rating, content, status, createdAt, updatedAt);
  }

  public void editContent(final int newRating, final String newContent) {
    validateRating(newRating);
    this.rating = newRating;
    this.content = newContent;
    this.updatedAt = Instant.now();
  }

  public void changeStatus(final ReviewStatus newStatus) {
    Objects.requireNonNull(newStatus, "newStatus must not be null");
    this.status = newStatus;
    this.updatedAt = Instant.now();
  }

  public boolean isOwnedBy(final UUID userId) {
    return this.userId.equals(userId);
  }

  private static void validateRating(final int rating) {
    if (rating < 1 || rating > 5) {
      throw new IllegalArgumentException("rating must be between 1 and 5, got: " + rating);
    }
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

  public UUID getBookId() {
    return bookId;
  }

  public int getRating() {
    return rating;
  }

  public String getContent() {
    return content;
  }

  public ReviewStatus getStatus() {
    return status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  @Override
  public boolean equals(final Object other) {
    if (this == other) return true;
    if (!(other instanceof Review review)) return false;
    return id != null && Objects.equals(id, review.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
