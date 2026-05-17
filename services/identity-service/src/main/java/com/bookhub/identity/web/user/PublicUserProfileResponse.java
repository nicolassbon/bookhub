package com.bookhub.identity.web.user;

import lombok.Builder;

@Builder
public record PublicUserProfileResponse(
    String userId, String username, String displayName, String bio, String avatarUrl) {}
