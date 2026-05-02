package com.bookhub.library.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_books")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserBookEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "book_id", nullable = false)
  private UUID bookId;

  @Column(name = "book_title", nullable = false)
  private String bookTitle;

  @Column(name = "book_cover_url")
  private String bookCoverUrl;

  @Column(name = "book_page_count")
  private Integer bookPageCount;

  @Enumerated(EnumType.STRING)
  @Column(name = "state", nullable = false, length = 20)
  private com.bookhub.library.domain.ReadingState state;

  @Column(name = "pages_read", nullable = false)
  private int pagesRead;

  @Column(name = "percentage")
  private Integer percentage;

  @Column(name = "started_at")
  private Instant startedAt;

  @Column(name = "finished_at")
  private Instant finishedAt;

  @Column(name = "added_at", nullable = false, updatable = false)
  private Instant addedAt;

  @Column(name = "last_progress_at")
  private Instant lastProgressAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

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

  @Override
  public boolean equals(final Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof UserBookEntity that)) {
      return false;
    }
    return id != null && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
