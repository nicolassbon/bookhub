package com.bookhub.library.web.admin;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookhub.library.application.admin.GetLibraryMetricsService;
import com.bookhub.library.application.admin.GetLibraryMetricsService.LibraryMetricsResult;
import com.bookhub.library.web.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminMetricsController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AdminMetricsControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private GetLibraryMetricsService getLibraryMetricsService;

  @Test
  void shouldReturnLibraryMetrics() throws Exception {
    final LibraryMetricsResult expected =
        new LibraryMetricsResult(42L, 150L, 30L, 50L, 70L, 15L, 120L, 10L, 120L, 4.2);

    when(getLibraryMetricsService.compute()).thenReturn(expected);

    mockMvc
        .perform(get("/api/v1/admin/metrics/library"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalUsers").value(42))
        .andExpect(jsonPath("$.totalLibraryEntries").value(150))
        .andExpect(jsonPath("$.entriesByState.wantToRead").value(30))
        .andExpect(jsonPath("$.entriesByState.reading").value(50))
        .andExpect(jsonPath("$.entriesByState.read").value(70))
        .andExpect(jsonPath("$.reviewsByStatus.pending").value(15))
        .andExpect(jsonPath("$.reviewsByStatus.approved").value(120))
        .andExpect(jsonPath("$.reviewsByStatus.rejected").value(10))
        .andExpect(jsonPath("$.totalReviews").value(120))
        .andExpect(jsonPath("$.averageRating").value(4.2));
  }
}
