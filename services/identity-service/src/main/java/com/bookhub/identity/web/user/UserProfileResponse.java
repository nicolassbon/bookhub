package com.bookhub.identity.web.user;

import lombok.Builder;

@Builder
public record UserProfileResponse(
        String userId,
        String username,
        String displayName,
        String email,
        String role) {
}
