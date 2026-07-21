package com.bookhub.library.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookhub.library.support.PostgreSqlIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class ActuatorSecurityIntegrationTest extends PostgreSqlIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void shouldExposeOnlyPrometheusAmongSensitiveActuatorEndpoints() throws Exception {
    mockMvc.perform(get("/actuator/prometheus")).andExpect(status().isOk());
    mockMvc.perform(get("/actuator/env")).andExpect(status().isUnauthorized());
  }
}
