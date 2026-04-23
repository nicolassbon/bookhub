package com.bookhub.identity.infrastructure.persistence;

import com.bookhub.identity.domain.auth.PasswordResetToken;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface PasswordResetTokenJpaRepository extends JpaRepository<PasswordResetToken, UUID> {

  Optional<PasswordResetToken> findByTokenHash(String tokenHash);

  List<PasswordResetToken> findAllByUserId(UUID userId);

  @Modifying
  @Transactional
  @Query(
      value =
          """
            INSERT INTO password_reset_tokens (id, token_hash, user_id, expires_at, created_at)
            VALUES (:id, :tokenHash, :userId, :expiresAt, CURRENT_TIMESTAMP)
            ON CONFLICT (user_id)
            DO UPDATE
            SET token_hash = EXCLUDED.token_hash,
                expires_at = EXCLUDED.expires_at,
                created_at = CURRENT_TIMESTAMP
            """,
      nativeQuery = true)
  void replaceForUser(
      @Param("id") UUID id,
      @Param("tokenHash") String tokenHash,
      @Param("userId") UUID userId,
      @Param("expiresAt") Instant expiresAt);

  @Modifying
  @Transactional
  @Query(
      value =
          """
            DELETE FROM password_reset_tokens
            WHERE token_hash = :tokenHash
              AND expires_at >= :now
            RETURNING user_id
            """,
      nativeQuery = true)
  List<UUID> consumeUserIdByTokenHash(
      @Param("tokenHash") String tokenHash, @Param("now") Instant now);

  @Transactional
  void deleteByUserId(UUID userId);
}
