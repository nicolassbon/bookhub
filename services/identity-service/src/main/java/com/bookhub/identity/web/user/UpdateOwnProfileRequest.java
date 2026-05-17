package com.bookhub.identity.web.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.net.URISyntaxException;

@JsonIgnoreProperties(ignoreUnknown = false)
public record UpdateOwnProfileRequest(
    @Size(max = 100, message = "displayName must be at most 100 characters") String displayName,
    @Size(max = 500, message = "bio must be at most 500 characters") String bio,
    @Size(max = 2048, message = "avatarUrl must be at most 2048 characters") String avatarUrl,
    String email,
    String username,
    String role,
    String status,
    String credentials) {

  @AssertTrue(message = "displayName must not be blank")
  public boolean isDisplayNameValid() {
    return displayName == null || !displayName.isBlank();
  }

  @AssertTrue(message = "avatarUrl must be a valid URL")
  public boolean isAvatarUrlValid() {
    if (avatarUrl == null || avatarUrl.isBlank()) {
      return avatarUrl == null;
    }

    try {
      final URI uri = new URI(avatarUrl);
      return uri.getScheme() != null
          && ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
          && uri.getHost() != null;
    } catch (URISyntaxException ignored) {
      return false;
    }
  }
}
