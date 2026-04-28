package com.bookhub.catalog.domain;

import java.util.UUID;

public class Book {

  private final UUID id;
  private final String title;
  private final String authorName;
  private final String isbn13;
  private final String sourceReference;
  private final String coverUrl;
  private final Integer publishedYear;
  private final Integer pageCount;

  private Book(
      final UUID id,
      final String title,
      final String authorName,
      final String isbn13,
      final String sourceReference,
      final String coverUrl,
      final Integer publishedYear,
      final Integer pageCount) {
    this.id = id;
    this.title = title;
    this.authorName = authorName;
    this.isbn13 = isbn13;
    this.sourceReference = sourceReference;
    this.coverUrl = coverUrl;
    this.publishedYear = publishedYear;
    this.pageCount = pageCount;
  }

  public static BookBuilder builder() {
    return new BookBuilder();
  }

  public UUID getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getAuthorName() {
    return authorName;
  }

  public String getIsbn13() {
    return isbn13;
  }

  public String getSourceReference() {
    return sourceReference;
  }

  public String getCoverUrl() {
    return coverUrl;
  }

  public Integer getPublishedYear() {
    return publishedYear;
  }

  public Integer getPageCount() {
    return pageCount;
  }

  public static final class BookBuilder {
    private UUID id;
    private String title;
    private String authorName;
    private String isbn13;
    private String sourceReference;
    private String coverUrl;
    private Integer publishedYear;
    private Integer pageCount;

    private BookBuilder() {}

    public BookBuilder id(final UUID id) {
      this.id = id;
      return this;
    }

    public BookBuilder title(final String title) {
      this.title = title;
      return this;
    }

    public BookBuilder authorName(final String authorName) {
      this.authorName = authorName;
      return this;
    }

    public BookBuilder isbn13(final String isbn13) {
      this.isbn13 = isbn13;
      return this;
    }

    public BookBuilder sourceReference(final String sourceReference) {
      this.sourceReference = sourceReference;
      return this;
    }

    public BookBuilder coverUrl(final String coverUrl) {
      this.coverUrl = coverUrl;
      return this;
    }

    public BookBuilder publishedYear(final Integer publishedYear) {
      this.publishedYear = publishedYear;
      return this;
    }

    public BookBuilder pageCount(final Integer pageCount) {
      this.pageCount = pageCount;
      return this;
    }

    public Book build() {
      return new Book(
          id, title, authorName, isbn13, sourceReference, coverUrl, publishedYear, pageCount);
    }
  }
}
