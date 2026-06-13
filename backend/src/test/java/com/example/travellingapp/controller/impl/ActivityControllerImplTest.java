package com.example.travellingapp.controller.impl;

import com.example.travellingapp.dto.request.create.CreateActivityDTO;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.response_template.ResponseBody;
import com.example.travellingapp.service.ActivityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Map;

import static com.example.travellingapp.enums.CommonEnum.ACTIVITY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ActivityControllerImplTest {

    private ActivityService activityService;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        activityService = mock(ActivityService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ActivityControllerImpl(activityService))
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void createActivity_shouldReturnServiceResponse() throws Exception {
        CreateActivityDTO request = new CreateActivityDTO();
        request.setActivityName("Museum Visit");
        request.setLocation("Adelaide Museum");
        request.setDescription("Visit museum");
        request.setStartDateTime(LocalDateTime.of(2026, 8, 1, 10, 0));
        request.setEndDateTime(LocalDateTime.of(2026, 8, 1, 12, 0));

        ResponseBody<Object> responseBody = new ResponseBody<>(
                "E000",
                "Activity created successfully",
                ACTIVITY.name(),
                Map.of("activityId", 1, "activityName", "Museum Visit")
        );

        when(activityService.createActivity(eq(1L), eq(2L), any(CreateActivityDTO.class)))
                .thenReturn(new CompleteResponse<>(responseBody, 200));

        mockMvc.perform(post("/api/v1/trips/1/destinations/2/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("E000"))
                .andExpect(jsonPath("$.flow").value(ACTIVITY.name()))
                .andExpect(jsonPath("$.body.activityId").value(1))
                .andExpect(jsonPath("$.body.activityName").value("Museum Visit"));

        verify(activityService).createActivity(eq(1L), eq(2L), any(CreateActivityDTO.class));
    }
}
