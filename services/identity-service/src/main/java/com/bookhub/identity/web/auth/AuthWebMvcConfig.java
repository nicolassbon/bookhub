package com.bookhub.identity.web.auth;

import com.bookhub.identity.config.AuthRateLimitProperties;
import com.bookhub.identity.web.auth.ratelimit.AuthRateLimitInterceptor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(AuthRateLimitProperties.class)
public class AuthWebMvcConfig implements WebMvcConfigurer {

  private final AuthRateLimitInterceptor authRateLimitInterceptor;

  public AuthWebMvcConfig(final AuthRateLimitInterceptor authRateLimitInterceptor) {
    this.authRateLimitInterceptor = authRateLimitInterceptor;
  }

  @Override
  public void addInterceptors(final InterceptorRegistry registry) {
    registry
        .addInterceptor(authRateLimitInterceptor)
        .addPathPatterns(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/refresh");
  }
}
