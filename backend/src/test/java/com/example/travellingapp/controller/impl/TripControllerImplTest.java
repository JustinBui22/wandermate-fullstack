package com.example.travellingapp.controller.impl;

import com.example.travellingapp.dto.request.create.CreateTripDTO;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.response_template.ResponseBody;
import com.example.travellingapp.service.TripService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Map;

import static com.example.travellingapp.enums.CommonEnum.TRIP;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TripControllerImplTest {

    private TripService tripService;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        tripService = mock(TripService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TripControllerImpl(tripService))
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void createTrip_shouldReturnCreatedTripResponse() throws Exception {
        CreateTripDTO request = new CreateTripDTO();
        request.setTripName("Adelaide Trip");
        request.setDestination("Adelaide");
        request.setStartDate(LocalDateTime.of(2026, 8, 1, 9, 0));
        request.setEndDate(LocalDateTime.of(2026, 8, 5, 18, 0));
        request.setAllowOverlap(false);

        ResponseBody<Object> responseBody = new ResponseBody<>(
                "E000",
                "Trip created successfully",
                TRIP.name(),
                Map.of("tripId", 1, "tripName", "Adelaide Trip")
        );

        when(tripService.createTrip(any(CreateTripDTO.class)))
                .thenReturn(new CompleteResponse<>(responseBody, 200));

        mockMvc.perform(post("/api/v1/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("E000"))
                .andExpect(jsonPath("$.message").value("Trip created successfully"))
                .andExpect(jsonPath("$.flow").value(TRIP.name()))
                .andExpect(jsonPath("$.body.tripId").value(1))
                .andExpect(jsonPath("$.body.tripName").value("Adelaide Trip"));

        verify(tripService).createTrip(any(CreateTripDTO.class));
    }

    @Test
    void createTrip_whenServiceReturnsConflict_shouldReturnConflictStatus() throws Exception {
        CreateTripDTO request = new CreateTripDTO();
        request.setTripName("Overlapping Trip");
        request.setDestination("Adelaide");
        request.setStartDate(LocalDateTime.of(2026, 8, 1, 9, 0));
        request.setEndDate(LocalDateTime.of(2026, 8, 5, 18, 0));
        request.setAllowOverlap(false);

        ResponseBody<Object> responseBody = new ResponseBody<>(
                "E051",
                "Trip date overlaps with an existing trip",
                TRIP.name(),
                null
        );

        when(tripService.createTrip(any(CreateTripDTO.class)))
                .thenReturn(new CompleteResponse<>(responseBody, 409));

        mockMvc.perform(post("/api/v1/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("E051"))
                .andExpect(jsonPath("$.message").value("Trip date overlaps with an existing trip"))
                .andExpect(jsonPath("$.flow").value(TRIP.name()));

        verify(tripService).createTrip(any(CreateTripDTO.class));
    }
}