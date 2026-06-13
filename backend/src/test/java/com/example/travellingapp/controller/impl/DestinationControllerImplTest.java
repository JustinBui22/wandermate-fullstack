package com.example.travellingapp.controller.impl;

import com.example.travellingapp.dto.request.create.CreateDestinationDTO;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.response_template.ResponseBody;
import com.example.travellingapp.service.DestinationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Map;

import static com.example.travellingapp.enums.CommonEnum.DESTINATION;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DestinationControllerImplTest {

    private DestinationService destinationService;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        destinationService = mock(DestinationService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new DestinationControllerImpl(destinationService))
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void createDestination_shouldReturnServiceResponse() throws Exception {
        CreateDestinationDTO request = new CreateDestinationDTO();
        request.setDestinationName("Adelaide CBD");
        request.setStartDate(LocalDateTime.of(2026, 8, 1, 9, 0));
        request.setEndDate(LocalDateTime.of(2026, 8, 3, 18, 0));
        request.setDestinationOrder(1);
        request.setNotes("Main city stop");
        request.setAllowOverlap(false);

        ResponseBody<Object> responseBody = new ResponseBody<>(
                "E000",
                "Destination created successfully",
                DESTINATION.name(),
                Map.of("destinationId", 1, "destinationName", "Adelaide CBD")
        );

        when(destinationService.createDestination(eq(1L), any(CreateDestinationDTO.class)))
                .thenReturn(new CompleteResponse<>(responseBody, 200));

        mockMvc.perform(post("/api/v1/trips/1/destinations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("E000"))
                .andExpect(jsonPath("$.flow").value(DESTINATION.name()))
                .andExpect(jsonPath("$.body.destinationId").value(1))
                .andExpect(jsonPath("$.body.destinationName").value("Adelaide CBD"));

        verify(destinationService).createDestination(eq(1L), any(CreateDestinationDTO.class));
    }
}
