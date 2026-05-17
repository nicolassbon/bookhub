package com.bookhub.identity.web.user;

import com.bookhub.identity.application.user.GetOwnProfileService;
import com.bookhub.identity.application.user.GetPublicProfileService;
import com.bookhub.identity.application.user.UpdateOwnProfileCommand;
import com.bookhub.identity.application.user.UpdateOwnProfileService;
import com.bookhub.identity.domain.user.User;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

  private final GetOwnProfileService getOwnProfileService;
  private final GetPublicProfileService getPublicProfileService;
  private final UpdateOwnProfileService updateOwnProfileService;

  @GetMapping("/me")
  public UserProfileResponse me(final JwtAuthenticationToken authentication) {
    final Jwt jwt = authentication.getToken();
    final User user = getOwnProfileService.execute(jwt.getSubject());

    return toSelfResponse(user);
  }

  @GetMapping("/{userId}")
  public PublicUserProfileResponse publicProfile(@PathVariable final UUID userId) {
    final User user = getPublicProfileService.execute(userId);

    return PublicUserProfileResponse.builder()
        .userId(user.getId().toString())
        .username(user.getUsername())
        .displayName(user.getDisplayName())
        .bio(user.getBio())
        .avatarUrl(user.getAvatarUrl())
        .build();
  }

  @PatchMapping("/me")
  public UserProfileResponse updateOwnProfile(
      final JwtAuthenticationToken authentication,
      @Valid @RequestBody final UpdateOwnProfileRequest request) {
    final Jwt jwt = authentication.getToken();

    final User updatedUser =
        updateOwnProfileService.execute(
            jwt.getSubject(),
            UpdateOwnProfileCommand.builder()
                .displayName(request.displayName())
                .bio(request.bio())
                .avatarUrl(request.avatarUrl())
                .bioProvided(request.bio() != null)
                .avatarUrlProvided(request.avatarUrl() != null)
                .email(request.email())
                .username(request.username())
                .role(request.role())
                .status(request.status())
                .credentials(request.credentials())
                .build());

    return toSelfResponse(updatedUser);
  }

  private UserProfileResponse toSelfResponse(final User user) {
    return UserProfileResponse.builder()
        .userId(user.getId().toString())
        .username(user.getUsername())
        .displayName(user.getDisplayName())
        .email(user.getEmail())
        .role(user.getRole().name())
        .bio(user.getBio())
        .avatarUrl(user.getAvatarUrl())
        .build();
  }
}
