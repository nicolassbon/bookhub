package com.bookhub.identity.application.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookhub.identity.domain.user.DuplicateResourceException;
import com.bookhub.identity.domain.user.User;
import com.bookhub.identity.domain.user.UserRepository;
import com.bookhub.identity.domain.user.UserRole;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthResultMapper authResultMapper;

    @InjectMocks
    private RegisterUserService registerUserService;

    @Test
    @DisplayName("Should register user when username and email are available")
    void shouldRegisterUserWhenUsernameAndEmailAreAvailable() {
        final RegisterUserCommand command = RegisterUserCommand.builder()
                .username("Nico")
                .email("NICO@Example.com")
                .password("StrongPassword123!")
                .displayName("Nicolas Bon")
                .build();

        when(userRepository.existsByEmail("nico@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("nico")).thenReturn(false);
        when(passwordEncoder.encode("StrongPassword123!")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            final User user = invocation.getArgument(0);
            return User.rehydrate(
                    UUID.fromString("6676f2d8-0f65-40ae-b102-66145e24f3fd"),
                    user.getUsername(),
                    user.getEmail(),
                    user.getPasswordHash(),
                    user.getDisplayName(),
                    user.getRole());
        });
        when(authResultMapper.toRegisterUserResult(any(User.class))).thenReturn(RegisterUserResult.builder()
                .userId("6676f2d8-0f65-40ae-b102-66145e24f3fd")
                .username("nico")
                .email("nico@example.com")
                .displayName("Nicolas Bon")
                .role(UserRole.USER.name())
                .build());

        final RegisterUserResult result = registerUserService.register(command);

        final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        final User persistedUser = userCaptor.getValue();
        assertThat(persistedUser.getUsername()).isEqualTo("nico");
        assertThat(persistedUser.getEmail()).isEqualTo("nico@example.com");
        assertThat(persistedUser.getPasswordHash()).isEqualTo("hashed-password");
        assertThat(persistedUser.getDisplayName()).isEqualTo("Nicolas Bon");
        assertThat(persistedUser.getRole()).isEqualTo(UserRole.USER);

        assertThat(result.userId()).isEqualTo("6676f2d8-0f65-40ae-b102-66145e24f3fd");
        assertThat(result.username()).isEqualTo("nico");
        assertThat(result.email()).isEqualTo("nico@example.com");
        assertThat(result.displayName()).isEqualTo("Nicolas Bon");
        assertThat(result.role()).isEqualTo(UserRole.USER.name());
    }

    @Test
    @DisplayName("Should reject registration when email already exists")
    void shouldRejectRegistrationWhenEmailAlreadyExists() {
        final RegisterUserCommand command = RegisterUserCommand.builder()
                .username("nico")
                .email("nico@example.com")
                .password("StrongPassword123!")
                .displayName("Nicolas Bon")
                .build();

        when(userRepository.existsByEmail("nico@example.com")).thenReturn(true);

        assertThatThrownBy(() -> registerUserService.register(command))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Email already in use");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should reject registration when username already exists")
    void shouldRejectRegistrationWhenUsernameAlreadyExists() {
        final RegisterUserCommand command = RegisterUserCommand.builder()
                .username("nico")
                .email("nico@example.com")
                .password("StrongPassword123!")
                .displayName("Nicolas Bon")
                .build();

        when(userRepository.existsByEmail("nico@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("nico")).thenReturn(true);

        assertThatThrownBy(() -> registerUserService.register(command))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Username already in use");

        verify(userRepository, never()).save(any(User.class));
    }
}
