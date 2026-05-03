package com.bookhub.library.web.notification;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookhub.library.application.GetNotificationService;
import com.bookhub.library.application.MarkNotificationAsReadService;
import com.bookhub.library.application.error.NotificationNotFoundException;
import com.bookhub.library.application.error.NotificationOwnershipException;
import com.bookhub.library.config.SecurityConfig;
import com.bookhub.library.domain.Notification;
import com.bookhub.library.domain.NotificationType;
import com.bookhub.library.web.GlobalExceptionHandler;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NotificationController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class NotificationControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private GetNotificationService getNotificationService;
  @MockitoBean private MarkNotificationAsReadService markNotificationAsReadService;
  @MockitoBean private JwtDecoder jwtDecoder;

  private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID NOTIF_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

  @Nested
  class GetNotifications {

    @Test
    void shouldReturn200AndListNotifications() throws Exception {
      final Notification notif =
          Notification.create(USER_ID, NotificationType.SYSTEM, "Welcome", "Hello!", "{}");
      notif.setId(NOTIF_ID);

      when(getNotificationService.forUser(USER_ID)).thenReturn(List.of(notif));

      mockMvc
          .perform(get("/api/v1/notifications").with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.length()").value(1))
          .andExpect(jsonPath("$[0].id").value(NOTIF_ID.toString()))
          .andExpect(jsonPath("$[0].type").value("SYSTEM"))
          .andExpect(jsonPath("$[0].title").value("Welcome"));
    }

    @Test
    void shouldReturn401WhenUnauthenticated() throws Exception {
      mockMvc.perform(get("/api/v1/notifications")).andExpect(status().isUnauthorized());
    }
  }

  @Nested
  class MarkAsRead {

    @Test
    void shouldReturn200WhenMarkedAsRead() throws Exception {
      mockMvc
          .perform(
              patch("/api/v1/notifications/{id}/read", NOTIF_ID)
                  .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
          .andExpect(status().isOk());

      verify(markNotificationAsReadService).execute(USER_ID, NOTIF_ID);
    }

    @Test
    void shouldReturn404WhenNotificationNotFound() throws Exception {
      doThrow(new NotificationNotFoundException("Not found"))
          .when(markNotificationAsReadService)
          .execute(USER_ID, NOTIF_ID);

      mockMvc
          .perform(
              patch("/api/v1/notifications/{id}/read", NOTIF_ID)
                  .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code").value("NOTIFICATION_NOT_FOUND"));
    }

    @Test
    void shouldReturn403WhenNotificationOwnedByAnotherUser() throws Exception {
      doThrow(new NotificationOwnershipException("Forbidden"))
          .when(markNotificationAsReadService)
          .execute(USER_ID, NOTIF_ID);

      mockMvc
          .perform(
              patch("/api/v1/notifications/{id}/read", NOTIF_ID)
                  .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.code").value("NOTIFICATION_OWNERSHIP_VIOLATION"));
    }

    @Test
    void shouldReturn401WhenUnauthenticated() throws Exception {
      mockMvc
          .perform(patch("/api/v1/notifications/{id}/read", NOTIF_ID))
          .andExpect(status().isUnauthorized());
    }
  }
}
