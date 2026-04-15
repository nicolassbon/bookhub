package com.bookhub.identity.application.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.bookhub.identity.domain.user.User;
import com.bookhub.identity.domain.user.UserRole;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class AuthResultMapperTest {

    private final AuthResultMapper mapper = Mappers.getMapper(AuthResultMapper.class);

    @Test
    void shouldMapUserToRegisterUserResult() {
        final User user = User.rehydrate(
                UUID.fromString("dcb37eff-06bc-4a28-b901-9be3bf3f99c2"),
                "nico",
                "nico@example.com",
                "hash",
                "Nicolas Bon",
                UserRole.USER);

        final RegisterUserResult result = mapper.toRegisterUserResult(user);

        assertThat(result.userId()).isEqualTo("dcb37eff-06bc-4a28-b901-9be3bf3f99c2");
        assertThat(result.username()).isEqualTo("nico");
        assertThat(result.email()).isEqualTo("nico@example.com");
        assertThat(result.displayName()).isEqualTo("Nicolas Bon");
        assertThat(result.role()).isEqualTo("USER");
    }

    @Test
    void shouldMapUserToLoginUserView() {
        final User user = User.rehydrate(
                UUID.fromString("3595f8f0-6a94-4d0c-8cab-a7978f2f7baa"),
                "ana",
                "ana@example.com",
                "hash",
                "Ana Torres",
                UserRole.ADMIN);

        final LoginUserResult.LoginUserView view = mapper.toLoginUserView(user);

        assertThat(view.userId()).isEqualTo("3595f8f0-6a94-4d0c-8cab-a7978f2f7baa");
        assertThat(view.username()).isEqualTo("ana");
        assertThat(view.displayName()).isEqualTo("Ana Torres");
        assertThat(view.role()).isEqualTo("ADMIN");
    }
}
