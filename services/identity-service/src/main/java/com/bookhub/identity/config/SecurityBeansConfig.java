package com.bookhub.identity.config;

import com.bookhub.identity.application.auth.PasswordResetTokenHasher;
import com.bookhub.identity.application.auth.RefreshTokenHasher;
import com.bookhub.identity.infrastructure.security.HmacPasswordResetTokenHasher;
import com.bookhub.identity.infrastructure.security.HmacRefreshTokenHasher;
import java.time.Clock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;

@Configuration
public class SecurityBeansConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public PasswordResetTokenHasher passwordResetTokenHasher(
            @Value("${auth.password-reset.hash-secret}") final String hashSecret) {
        return new HmacPasswordResetTokenHasher(hashSecret);
    }

    @Bean
    public RefreshTokenHasher refreshTokenHasher(
            @Value("${auth.refresh-token.hash-secret}") final String hashSecret) {
        return new HmacRefreshTokenHasher(hashSecret);
    }
}
