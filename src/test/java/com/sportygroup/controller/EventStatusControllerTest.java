package com.sportygroup.controller;

import com.sportygroup.service.impl.EventLifecycleServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for the REST API endpoint.
 */
@WebMvcTest(controllers = EventStatusController.class)
class EventStatusControllerTest {

  @Autowired
  private MockMvc mvc;

  @MockBean
  private EventLifecycleServiceImpl lifecycleService;

  @Test
  void shouldAcceptLiveStatusAndTriggerScheduler() throws Exception {
    mvc.perform(post("/events/status")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"eventId\":\"1234\",\"status\":\"LIVE\"}"))
      .andExpect(status().isAccepted());

    verify(lifecycleService, times(1)).markLive("1234");
    verify(lifecycleService, never()).markNotLive(anyString());
  }

  @Test
  void shouldRejectInvalidPayload() throws Exception {
    // missing eventId and status
    mvc.perform(post("/events/status")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{}"))
      .andExpect(status().isBadRequest());

    verifyNoInteractions(lifecycleService);
  }
}
