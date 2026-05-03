package com.bookhub.library.web.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookhub.library.domain.Notification;
import com.bookhub.library.domain.NotificationStatus;
import com.bookhub.library.domain.NotificationType;
import com.bookhub.library.infrastructure.persistence.JpaNotificationRepository;
import com.bookhub.library.infrastructure.persistence.NotificationEntity;
import com.bookhub.library.infrastructure.persistence.NotificationEntityMapper;
import com.bookhub.library.support.PostgreSqlIntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class NotificationIntegrationTest extends PostgreSqlIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private JpaNotificationRepository jpaNotificationRepository;
  @Autowired private NotificationEntityMapper mapper;

  private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000300");

  private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor authenticatedJwt() {
    return jwt().jwt(builder -> builder.subject(USER_ID.toString()));
  }

  @Test
  @DisplayName("Full Notification Lifecycle: List unread -> Mark read -> List read")
  void fullNotificationLifecycle() throws Exception {
    // 1. Setup raw DB state directly using repository
    final Notification notif1 =
        Notification.create(USER_ID, NotificationType.SYSTEM, "Welcome", "Hello!", "{}");
    final NotificationEntity savedEntity = jpaNotificationRepository.save(mapper.toEntity(notif1));
    final UUID notifId = savedEntity.getId();

    // 2. Fetch notifications
    mockMvc
        .perform(get("/api/v1/notifications").with(authenticatedJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(notifId.toString()))
        .andExpect(jsonPath("$[0].status").value("UNREAD"));

    // 3. Mark as read
    mockMvc
        .perform(patch("/api/v1/notifications/{id}/read", notifId).with(authenticatedJwt()))
        .andExpect(status().isOk());

    // 4. Fetch again, should be READ
    mockMvc
        .perform(get("/api/v1/notifications").with(authenticatedJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(notifId.toString()))
        .andExpect(jsonPath("$[0].status").value("READ"));

    // 5. Verify DB state
    assertThat(jpaNotificationRepository.findById(notifId))
        .isPresent()
        .hasValueSatisfying(n -> assertThat(n.getStatus()).isEqualTo(NotificationStatus.READ));
  }
}
