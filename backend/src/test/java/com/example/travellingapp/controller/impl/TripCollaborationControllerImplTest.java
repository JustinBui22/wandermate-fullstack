package com.example.travellingapp.controller.impl;

import com.example.travellingapp.dto.request.SendTripInvitationDTO;
import com.example.travellingapp.dto.request.SendTripJoinRequestDTO;
import com.example.travellingapp.enums.TripCollaborationEnum;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.response_template.ResponseBody;
import com.example.travellingapp.service.TripCollaborationRequestService;
import com.example.travellingapp.service.TripOverlapWarningService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static com.example.travellingapp.enums.CommonEnum.TRIP_MEMBER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TripCollaborationControllerImplTest {

    private TripCollaborationRequestService tripCollaborationRequestService;
    private TripOverlapWarningService tripOverlapWarningService;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        tripCollaborationRequestService = mock(TripCollaborationRequestService.class);
        tripOverlapWarningService = mock(TripOverlapWarningService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new TripCollaborationControllerImpl(
                        tripCollaborationRequestService,
                        tripOverlapWarningService
                ))
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void sendInvitation_shouldReturnServiceResponse() throws Exception {
        SendTripInvitationDTO request = new SendTripInvitationDTO();
        request.setUsername("FriendUser");
        request.setRole(TripCollaborationEnum.EDITOR);

        ResponseBody<Object> responseBody = new ResponseBody<>(
                "E000",
                "Trip invitation sent successfully",
                TRIP_MEMBER.name(),
                Map.of(
                        "requestId", 5,
                        "targetUsername", "FriendUser",
                        "requestedRole", "EDITOR",
                        "status", "PENDING"
                )
        );

        when(tripCollaborationRequestService.sendInvitation(eq(1L), any(SendTripInvitationDTO.class)))
                .thenReturn(new CompleteResponse<>(responseBody, 200));

        mockMvc.perform(post("/api/v1/trips/1/invitations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("E000"))
                .andExpect(jsonPath("$.message").value("Trip invitation sent successfully"))
                .andExpect(jsonPath("$.flow").value(TRIP_MEMBER.name()))
                .andExpect(jsonPath("$.body.requestId").value(5))
                .andExpect(jsonPath("$.body.targetUsername").value("FriendUser"))
                .andExpect(jsonPath("$.body.status").value("PENDING"));

        verify(tripCollaborationRequestService)
                .sendInvitation(eq(1L), any(SendTripInvitationDTO.class));
    }

    @Test
    void getMyPendingInvitations_shouldReturnServiceResponse() throws Exception {
        ResponseBody<Object> responseBody = new ResponseBody<>(
                "E000",
                "Trip invitations retrieved successfully",
                TRIP_MEMBER.name(),
                List.of(
                        Map.of(
                                "requestId", 5,
                                "tripName", "Adelaide Trip",
                                "requestType", "INVITATION",
                                "status", "PENDING"
                        )
                )
        );

        when(tripCollaborationRequestService.getMyPendingInvitations())
                .thenReturn(new CompleteResponse<>(responseBody, 200));

        mockMvc.perform(get("/api/v1/trips/invitations/received")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("E000"))
                .andExpect(jsonPath("$.body[0].requestId").value(5))
                .andExpect(jsonPath("$.body[0].tripName").value("Adelaide Trip"));

        verify(tripCollaborationRequestService).getMyPendingInvitations();
    }

    @Test
    void acceptInvitation_shouldReturnServiceResponse() throws Exception {
        ResponseBody<Object> responseBody = new ResponseBody<>(
                "E000",
                "Trip invitation accepted successfully",
                TRIP_MEMBER.name(),
                Map.of(
                        "request", Map.of("requestId", 5, "status", "ACCEPTED"),
                        "member", Map.of("tripMemberId", 2, "username", "FriendUser", "role", "EDITOR"),
                        "overlapWarnings", List.of()
                )
        );

        when(tripCollaborationRequestService.acceptInvitation(5L))
                .thenReturn(new CompleteResponse<>(responseBody, 200));

        mockMvc.perform(patch("/api/v1/trips/invitations/5/accept")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("E000"))
                .andExpect(jsonPath("$.body.request.requestId").value(5))
                .andExpect(jsonPath("$.body.request.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.body.member.username").value("FriendUser"));

        verify(tripCollaborationRequestService).acceptInvitation(5L);
    }

    @Test
    void rejectInvitation_shouldReturnServiceResponse() throws Exception {
        ResponseBody<Object> responseBody = new ResponseBody<>(
                "E000",
                "Trip invitation rejected successfully",
                TRIP_MEMBER.name(),
                Map.of("requestId", 5, "status", "REJECTED")
        );

        when(tripCollaborationRequestService.rejectInvitation(5L))
                .thenReturn(new CompleteResponse<>(responseBody, 200));

        mockMvc.perform(patch("/api/v1/trips/invitations/5/reject")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("E000"))
                .andExpect(jsonPath("$.body.requestId").value(5))
                .andExpect(jsonPath("$.body.status").value("REJECTED"));

        verify(tripCollaborationRequestService).rejectInvitation(5L);
    }

    @Test
    void requestToJoinTrip_shouldReturnServiceResponse() throws Exception {
        SendTripJoinRequestDTO request = new SendTripJoinRequestDTO();
        request.setRole(TripCollaborationEnum.VIEWER);

        ResponseBody<Object> responseBody = new ResponseBody<>(
                "E000",
                "Trip join request sent successfully",
                TRIP_MEMBER.name(),
                Map.of(
                        "requestId", 9,
                        "requesterUsername", "FriendUser",
                        "requestedRole", "VIEWER",
                        "status", "PENDING"
                )
        );

        when(tripCollaborationRequestService.requestToJoinTrip(eq(1L), any(SendTripJoinRequestDTO.class)))
                .thenReturn(new CompleteResponse<>(responseBody, 200));

        mockMvc.perform(post("/api/v1/trips/1/join-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("E000"))
                .andExpect(jsonPath("$.body.requestId").value(9))
                .andExpect(jsonPath("$.body.requesterUsername").value("FriendUser"));

        verify(tripCollaborationRequestService)
                .requestToJoinTrip(eq(1L), any(SendTripJoinRequestDTO.class));
    }

    @Test
    void getPendingJoinRequests_shouldReturnServiceResponse() throws Exception {
        ResponseBody<Object> responseBody = new ResponseBody<>(
                "E000",
                "Trip join requests retrieved successfully",
                TRIP_MEMBER.name(),
                List.of(
                        Map.of(
                                "requestId", 9,
                                "requesterUsername", "FriendUser",
                                "status", "PENDING"
                        )
                )
        );

        when(tripCollaborationRequestService.getPendingJoinRequests(1L))
                .thenReturn(new CompleteResponse<>(responseBody, 200));

        mockMvc.perform(get("/api/v1/trips/1/join-requests")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body[0].requestId").value(9))
                .andExpect(jsonPath("$.body[0].requesterUsername").value("FriendUser"));

        verify(tripCollaborationRequestService).getPendingJoinRequests(1L);
    }

    @Test
    void acceptJoinRequest_shouldReturnServiceResponse() throws Exception {
        ResponseBody<Object> responseBody = new ResponseBody<>(
                "E000",
                "Trip join request accepted successfully",
                TRIP_MEMBER.name(),
                Map.of(
                        "request", Map.of("requestId", 9, "status", "ACCEPTED"),
                        "member", Map.of("tripMemberId", 3, "username", "FriendUser", "role", "VIEWER"),
                        "overlapWarnings", List.of()
                )
        );

        when(tripCollaborationRequestService.acceptJoinRequest(9L))
                .thenReturn(new CompleteResponse<>(responseBody, 200));

        mockMvc.perform(patch("/api/v1/trips/join-requests/9/accept")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.request.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.body.member.username").value("FriendUser"));

        verify(tripCollaborationRequestService).acceptJoinRequest(9L);
    }

    @Test
    void rejectJoinRequest_shouldReturnServiceResponse() throws Exception {
        ResponseBody<Object> responseBody = new ResponseBody<>(
                "E000",
                "Trip join request rejected successfully",
                TRIP_MEMBER.name(),
                Map.of("requestId", 9, "status", "REJECTED")
        );

        when(tripCollaborationRequestService.rejectJoinRequest(9L))
                .thenReturn(new CompleteResponse<>(responseBody, 200));

        mockMvc.perform(patch("/api/v1/trips/join-requests/9/reject")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.requestId").value(9))
                .andExpect(jsonPath("$.body.status").value("REJECTED"));

        verify(tripCollaborationRequestService).rejectJoinRequest(9L);
    }

    @Test
    void getMyOverlapWarnings_shouldReturnServiceResponse() throws Exception {
        ResponseBody<Object> responseBody = new ResponseBody<>(
                "E000",
                "Trip overlap warnings retrieved successfully",
                TRIP_MEMBER.name(),
                List.of(
                        Map.of(
                                "overlappingTripId", 2,
                                "overlappingTripName", "Melbourne Trip",
                                "message", "This shared trip overlaps with your trip \"Melbourne Trip\"."
                        )
                )
        );

        when(tripOverlapWarningService.getMyOverlapWarnings(1L))
                .thenReturn(new CompleteResponse<>(responseBody, 200));

        mockMvc.perform(get("/api/v1/trips/1/my-overlap-warnings")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body[0].overlappingTripId").value(2))
                .andExpect(jsonPath("$.body[0].overlappingTripName").value("Melbourne Trip"));

        verify(tripOverlapWarningService).getMyOverlapWarnings(1L);
    }

    @Test
    void acceptInvitation_whenServiceReturnsForbidden_shouldReturnForbidden() throws Exception {
        ResponseBody<Object> responseBody = new ResponseBody<>(
                "E064",
                "You do not have permission to access this trip",
                TRIP_MEMBER.name(),
                null
        );

        when(tripCollaborationRequestService.acceptInvitation(5L))
                .thenReturn(new CompleteResponse<>(responseBody, 403));

        mockMvc.perform(patch("/api/v1/trips/invitations/5/accept")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("E064"))
                .andExpect(jsonPath("$.flow").value(TRIP_MEMBER.name()));

        verify(tripCollaborationRequestService).acceptInvitation(5L);
    }
}