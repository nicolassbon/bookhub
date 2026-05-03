package com.bookhub.library.domain;

import java.time.Instant;
import java.time.Year;
import java.util.Objects;
import java.util.UUID;
import lombok.Builder;

public class YearlyGoal {

  private UUID id;
  private final UUID userId;
  private final int year;
  private int targetBooks;
  private int completedBooks;
  private GoalStatus status;
  private final Instant createdAt;
  private Instant updatedAt;

  private YearlyGoal(
      final UUID id,
      final UUID userId,
      final int year,
      final int targetBooks,
      final int completedBooks,
      final GoalStatus status,
      final Instant createdAt,
      final Instant updatedAt) {
    this.id = id;
    this.userId = userId;
    this.year = year;
    this.targetBooks = targetBooks;
    this.completedBooks = completedBooks;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static YearlyGoal create(final UUID userId, final int year, final int targetBooks) {
    Objects.requireNonNull(userId, "userId must not be null");
    validateYear(year);
    validateTargetBooks(targetBooks);
    final Instant now = Instant.now();
    return new YearlyGoal(null, userId, year, targetBooks, 0, GoalStatus.IN_PROGRESS, now, now);
  }

  @Builder(builderMethodName = "rehydrateBuilder")
  public static YearlyGoal rehydrate(
      final UUID id,
      final UUID userId,
      final int year,
      final int targetBooks,
      final int completedBooks,
      final GoalStatus status,
      final Instant createdAt,
      final Instant updatedAt) {
    return new YearlyGoal(
        id, userId, year, targetBooks, completedBooks, status, createdAt, updatedAt);
  }

  public void updateTarget(final int newTargetBooks) {
    validateTargetBooks(newTargetBooks);
    this.targetBooks = newTargetBooks;
    this.status =
        this.completedBooks >= this.targetBooks ? GoalStatus.ACHIEVED : GoalStatus.IN_PROGRESS;
    this.updatedAt = Instant.now();
  }

  /**
   * Increments the completed books counter. Idempotent once ACHIEVED — additional completions in
   * the same year do not inflate the counter beyond target.
   */
  public void incrementProgress() {
    if (this.status == GoalStatus.ACHIEVED) {
      return;
    }
    this.completedBooks++;
    this.updatedAt = Instant.now();
    if (this.completedBooks >= this.targetBooks) {
      this.status = GoalStatus.ACHIEVED;
    }
  }

  public boolean isAchieved() {
    return this.status == GoalStatus.ACHIEVED;
  }

  private static void validateTargetBooks(final int targetBooks) {
    if (targetBooks <= 0) {
      throw new IllegalArgumentException(
          "targetBooks must be greater than zero, got: " + targetBooks);
    }
  }

  private static void validateYear(final int year) {
    final int minYear = 1900;
    final int maxYear = Year.now().getValue() + 10;
    if (year < minYear || year > maxYear) {
      throw new IllegalArgumentException(
          "year must be between " + minYear + " and " + maxYear + ", got: " + year);
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

  public int getYear() {
    return year;
  }

  public int getTargetBooks() {
    return targetBooks;
  }

  public int getCompletedBooks() {
    return completedBooks;
  }

  public GoalStatus getStatus() {
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
    if (!(other instanceof YearlyGoal goal)) return false;
    return id != null && Objects.equals(id, goal.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
