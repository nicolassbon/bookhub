package com.bookhub.library.web.notification;

import com.bookhub.library.application.GetNotificationService;
import com.bookhub.library.application.MarkNotificationAsReadService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final GetNotificationService getNotificationService;
  private final MarkNotificationAsReadService markNotificationAsReadService;

  @GetMapping
  public ResponseEntity<List<NotificationResponse>> getNotifications(
      @AuthenticationPrincipal final Jwt jwt) {
    final UUID userId = UUID.fromString(jwt.getSubject());
    final List<NotificationResponse> responses =
        getNotificationService.forUser(userId).stream().map(NotificationResponse::from).toList();
    return ResponseEntity.ok(responses);
  }

  @PatchMapping("/{id}/read")
  public ResponseEntity<Void> markAsRead(
      @PathVariable final UUID id, @AuthenticationPrincipal final Jwt jwt) {
    final UUID userId = UUID.fromString(jwt.getSubject());
    markNotificationAsReadService.execute(userId, id);
    return ResponseEntity.ok().build();
  }
}
