package com.bookhub.library.web.admin;

public record LibraryMetricsResponse(
    long totalUsers,
    long totalLibraryEntries,
    StateCounts entriesByState,
    ReviewCounts reviewsByStatus,
    long totalReviews,
    double averageRating) {

  public record StateCounts(long wantToRead, long reading, long read) {}

  public record ReviewCounts(long pending, long approved, long rejected) {}
}
