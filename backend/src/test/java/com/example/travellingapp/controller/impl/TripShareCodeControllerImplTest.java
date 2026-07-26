package com.example.travellingapp.controller.impl;

import com.example.travellingapp.dto.request.PreviewTripShareCodeRequest;
import com.example.travellingapp.dto.request.create.GenerateTripShareCodeRequest;
import com.example.travellingapp.enums.TripEnum;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.response_template.ResponseBody;
import com.example.travellingapp.service.TripShareCodeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static com.example.travellingapp.enums.CommonEnum.TRIP_MEMBER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TripShareCodeControllerImplTest {

    private TripShareCodeService tripShareCodeService;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        tripShareCodeService = mock(TripShareCodeService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new TripShareCodeControllerImpl(tripShareCodeService))
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void regenerateShareCode_shouldReturnServiceResponse() throws Exception {
        GenerateTripShareCodeRequest request = new GenerateTripShareCodeRequest();
        request.setDefaultRole(TripEnum.VIEWER);

        ResponseBody<Object> responseBody = new ResponseBody<>(
                "E000",
                "Trip share code created successfully",
                TRIP_MEMBER.name(),
                Map.of(
                        "tripId", 1,
                        "tripName", "Adelaide Trip",
                        "code", "WM-ABCDEFGHJKLM",
                        "inviteLink", "wandermate://join-trip?code=WM-ABCDEFGHJKLM",
                        "defaultRole", "VIEWER",
                        "codeStatus", "ACTIVE"
                )
        );

        when(tripShareCodeService.regenerateShareCode(
                eq(1L),
                any(GenerateTripShareCodeRequest.class)
        )).thenReturn(new CompleteResponse<>(responseBody, 200));

        mockMvc.perform(post("/api/v1/trips/1/share-codes/regenerate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("E000"))
                .andExpect(jsonPath("$.message").value("Trip share code created successfully"))
                .andExpect(jsonPath("$.flow").value(TRIP_MEMBER.name()))
                .andExpect(jsonPath("$.body.tripId").value(1))
                .andExpect(jsonPath("$.body.tripName").value("Adelaide Trip"))
                .andExpect(jsonPath("$.body.code").value("WM-ABCDEFGHJKLM"))
                .andExpect(jsonPath("$.body.defaultRole").value("VIEWER"))
                .andExpect(jsonPath("$.body.codeStatus").value("ACTIVE"));

        verify(tripShareCodeService).regenerateShareCode(
                eq(1L),
                any(GenerateTripShareCodeRequest.class)
        );
    }

    @Test
    void previewShareCode_shouldReturnServiceResponse() throws Exception {
        ResponseBody<Object> responseBody = new ResponseBody<>(
                "E000",
                "Trip share code retrieved successfully",
                TRIP_MEMBER.name(),
                Map.of(
                        "tripId", 1,
                        "tripName", "Adelaide Trip",
                        "destination", "Adelaide",
                        "ownerUsername", "OwnerUser",
                        "defaultRole", "VIEWER"
                )
        );

        when(tripShareCodeService.previewShareCode("WM-ABCDEFGHJKLM"))
                .thenReturn(new CompleteResponse<>(responseBody, 200));

        PreviewTripShareCodeRequest request = new PreviewTripShareCodeRequest();
        request.setCode("WM-ABCDEFGHJKLM");

        mockMvc.perform(post("/api/v1/trips/share-codes/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("E000"))
                .andExpect(jsonPath("$.message").value("Trip share code retrieved successfully"))
                .andExpect(jsonPath("$.flow").value(TRIP_MEMBER.name()))
                .andExpect(jsonPath("$.body.tripId").value(1))
                .andExpect(jsonPath("$.body.tripName").value("Adelaide Trip"))
                .andExpect(jsonPath("$.body.destination").value("Adelaide"))
                .andExpect(jsonPath("$.body.ownerUsername").value("OwnerUser"))
                .andExpect(jsonPath("$.body.defaultRole").value("VIEWER"));

        verify(tripShareCodeService).previewShareCode("WM-ABCDEFGHJKLM");
    }

    @Test
    void requestToJoinByShareCode_shouldReturnServiceResponse() throws Exception {
        ResponseBody<Object> responseBody = new ResponseBody<>(
                "E000",
                "Trip share code join request sent successfully",
                TRIP_MEMBER.name(),
                Map.of(
                        "requestId", 9,
                        "tripId", 1,
                        "tripName", "Adelaide Trip",
                        "requesterUsername", "FriendUser",
                        "targetUsername", "OwnerUser",
                        "requestedRole", "VIEWER",
                        "requestType", "JOIN_REQUEST",
                        "status", "PENDING"
                )
        );

        when(tripShareCodeService.requestToJoinByShareCode("WM-ABCDEFGHJKLM"))
                .thenReturn(new CompleteResponse<>(responseBody, 200));

        mockMvc.perform(post("/api/v1/trips/share-codes/WM-ABCDEFGHJKLM/join-requests")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("E000"))
                .andExpect(jsonPath("$.message").value("Trip share code join request sent successfully"))
                .andExpect(jsonPath("$.flow").value(TRIP_MEMBER.name()))
                .andExpect(jsonPath("$.body.requestId").value(9))
                .andExpect(jsonPath("$.body.tripName").value("Adelaide Trip"))
                .andExpect(jsonPath("$.body.requesterUsername").value("FriendUser"))
                .andExpect(jsonPath("$.body.requestedRole").value("VIEWER"))
                .andExpect(jsonPath("$.body.status").value("PENDING"));

        verify(tripShareCodeService).requestToJoinByShareCode("WM-ABCDEFGHJKLM");
    }

    @Test
    void regenerateShareCode_whenServiceReturnsTooManyRequests_shouldReturnTooManyRequestsStatus() throws Exception {
        ResponseBody<Object> responseBody = new ResponseBody<>(
                "E083",
                "Please wait before generating another trip share code",
                TRIP_MEMBER.name(),
                null
        );

        when(tripShareCodeService.regenerateShareCode(
                eq(1L),
                any(GenerateTripShareCodeRequest.class)
        )).thenReturn(new CompleteResponse<>(responseBody, 429));

        mockMvc.perform(post("/api/v1/trips/1/share-codes/regenerate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GenerateTripShareCodeRequest())))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("E083"))
                .andExpect(jsonPath("$.message").value("Please wait before generating another trip share code"))
                .andExpect(jsonPath("$.flow").value(TRIP_MEMBER.name()));

        verify(tripShareCodeService).regenerateShareCode(
                eq(1L),
                any(GenerateTripShareCodeRequest.class)
        );
    }
}