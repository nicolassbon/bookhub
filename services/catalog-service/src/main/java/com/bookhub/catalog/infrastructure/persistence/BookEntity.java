package com.bookhub.catalog.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "books")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookEntity {

  @Id private UUID id;

  @Column(name = "title", nullable = false, length = 255)
  private String title;

  @Column(name = "author_name", length = 255)
  private String authorName;

  @Column(name = "isbn13", length = 13)
  private String isbn13;

  @Column(name = "source_reference", nullable = false, length = 100)
  private String sourceReference;

  @Column(name = "cover_url", length = 500)
  private String coverUrl;

  @Column(name = "published_year")
  private Integer publishedYear;

  @Column(name = "page_count")
  private Integer pageCount;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

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

  public UUID getId() {
    return id;
  }

  public void setId(final UUID id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

  public String getAuthorName() {
    return authorName;
  }

  public void setAuthorName(final String authorName) {
    this.authorName = authorName;
  }

  public String getIsbn13() {
    return isbn13;
  }

  public void setIsbn13(final String isbn13) {
    this.isbn13 = isbn13;
  }

  public String getSourceReference() {
    return sourceReference;
  }

  public void setSourceReference(final String sourceReference) {
    this.sourceReference = sourceReference;
  }

  public String getCoverUrl() {
    return coverUrl;
  }

  public void setCoverUrl(final String coverUrl) {
    this.coverUrl = coverUrl;
  }

  public Integer getPublishedYear() {
    return publishedYear;
  }

  public void setPublishedYear(final Integer publishedYear) {
    this.publishedYear = publishedYear;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(final Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(final Instant updatedAt) {
    this.updatedAt = updatedAt;
  }

  public Integer getPageCount() {
    return pageCount;
  }

  public void setPageCount(final Integer pageCount) {
    this.pageCount = pageCount;
  }

  @Override
  public boolean equals(final Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof BookEntity that)) {
      return false;
    }
    return id != null && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
