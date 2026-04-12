package com.bookhub.identity.infrastructure.persistence;

import com.bookhub.identity.domain.auth.RefreshToken;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenAndRevokedFalseAndExpiresAtAfter(UUID token, Instant now);
}
