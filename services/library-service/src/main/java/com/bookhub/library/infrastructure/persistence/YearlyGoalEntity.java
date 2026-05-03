package com.bookhub.library.infrastructure.persistence;

import com.bookhub.library.domain.GoalStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "yearly_goals",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_yearly_goals_user_year",
            columnNames = {"user_id", "year"}))
@Getter
@Setter
@NoArgsConstructor
public class YearlyGoalEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(nullable = false)
  private int year;

  @Column(name = "target_books", nullable = false)
  private int targetBooks;

  @Column(name = "completed_books", nullable = false)
  private int completedBooks;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private GoalStatus status;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
