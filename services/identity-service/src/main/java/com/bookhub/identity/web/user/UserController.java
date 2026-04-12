package com.bookhub.identity.web.user;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @GetMapping("/me")
    public UserProfileResponse me(@AuthenticationPrincipal final Jwt jwt) {
        return UserProfileResponse.builder()
                .userId(jwt.getSubject())
                .username(claim(jwt, "username"))
                .displayName(claim(jwt, "displayName"))
                .email(claim(jwt, "email"))
                .role(claim(jwt, "role"))
                .build();
    }

    private String claim(final Jwt jwt, final String key) {
        return jwt.getClaimAsString(key);
    }
}
