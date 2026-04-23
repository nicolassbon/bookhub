package com.bookhub.identity.support;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.bookhub.identity.infrastructure.persistence.PasswordResetTokenJpaRepository;
import com.bookhub.identity.infrastructure.persistence.RefreshTokenJpaRepository;
import com.bookhub.identity.infrastructure.persistence.UserJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IdentityDatabaseCleanerTest {

  @Mock private RefreshTokenJpaRepository refreshTokenJpaRepository;

  @Mock private PasswordResetTokenJpaRepository passwordResetTokenJpaRepository;

  @Mock private UserJpaRepository userJpaRepository;

  @InjectMocks private IdentityDatabaseCleaner identityDatabaseCleaner;

  @Test
  @DisplayName("Should delete tables in foreign-key-safe order")
  void shouldDeleteTablesInForeignKeySafeOrder() {
    identityDatabaseCleaner.clean();

    final InOrder orderedDeletion =
        inOrder(refreshTokenJpaRepository, passwordResetTokenJpaRepository, userJpaRepository);

    orderedDeletion.verify(refreshTokenJpaRepository).deleteAll();
    orderedDeletion.verify(passwordResetTokenJpaRepository).deleteAll();
    orderedDeletion.verify(userJpaRepository).deleteAll();
  }

  @Test
  @DisplayName("Should run one cleanup call per repository")
  void shouldRunOneCleanupCallPerRepository() {
    identityDatabaseCleaner.clean();

    verify(refreshTokenJpaRepository, times(1)).deleteAll();
    verify(passwordResetTokenJpaRepository, times(1)).deleteAll();
    verify(userJpaRepository, times(1)).deleteAll();
    verifyNoMoreInteractions(
        refreshTokenJpaRepository, passwordResetTokenJpaRepository, userJpaRepository);
  }
}
