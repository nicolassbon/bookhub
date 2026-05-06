package com.bookhub.library.infrastructure.persistence;

import com.bookhub.library.domain.PaginatedResult;
import com.bookhub.library.domain.PaginationQuery;
import com.bookhub.library.domain.Review;
import com.bookhub.library.domain.ReviewRepository;
import com.bookhub.library.domain.ReviewStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewRepositoryAdapter implements ReviewRepository {

  private final JpaReviewRepository jpaRepository;
  private final ReviewEntityMapper mapper;

  @Override
  public Optional<Review> findByUserIdAndBookId(final UUID userId, final UUID bookId) {
    return jpaRepository.findByUserIdAndBookId(userId, bookId).map(mapper::toDomain);
  }

  @Override
  public List<Review> findByBookId(final UUID bookId) {
    return jpaRepository.findByBookId(bookId).stream().map(mapper::toDomain).toList();
  }

  @Override
  public List<Review> findByBookIdAndStatus(final UUID bookId, final ReviewStatus status) {
    return jpaRepository.findByBookIdAndStatus(bookId, status).stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Override
  public Optional<Review> findById(final UUID reviewId) {
    return jpaRepository.findById(reviewId).map(mapper::toDomain);
  }

  @Override
  public Review save(final Review review) {
    final ReviewEntity entity = mapper.toEntity(review);
    final ReviewEntity saved = jpaRepository.save(entity);
    review.setId(saved.getId());
    return mapper.toDomain(saved);
  }

  @Override
  public PaginatedResult<Review> findAllForModeration(final PaginationQuery pagination) {
    final Pageable pageable = PageRequest.of(pagination.page(), pagination.size());
    final org.springframework.data.domain.Page<ReviewEntity> page = jpaRepository.findAll(pageable);
    return new PaginatedResult<>(
        page.stream().map(mapper::toDomain).toList(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages());
  }
}
