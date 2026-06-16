package com.example.travellingapp.controller.impl;

import com.example.travellingapp.dto.request.AddTripMemberDTO;
import com.example.travellingapp.dto.request.update.UpdateTripMemberRoleDTO;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.response_template.ResponseBody;
import com.example.travellingapp.service.TripMemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TripMemberControllerImplTest {

    private TripMemberService tripMemberService;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        tripMemberService = mock(TripMemberService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new TripMemberControllerImpl(tripMemberService))
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void getTripMembers_shouldReturnServiceResponse() throws Exception {
        ResponseBody<Object> responseBody = new ResponseBody<>(
                "E000",
                "Trip members retrieved successfully",
                TRIP_MEMBER.name(),
                List.of(
                        Map.of(
                                "tripMemberId", 1,
                                "username", "JustinBo123",
                                "role", "OWNER"
                        ),
                        Map.of(
                                "tripMemberId", 2,
                                "username", "FriendUser",
                                "role", "EDITOR"
                        )
                )
        );

        when(tripMemberService.getTripMembers(1L))
                .thenReturn(new CompleteResponse<>(responseBody, 200));

        mockMvc.perform(get("/api/v1/trips/1/members")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("E000"))
                .andExpect(jsonPath("$.message").value("Trip members retrieved successfully"))
                .andExpect(jsonPath("$.flow").value(TRIP_MEMBER.name()))
                .andExpect(jsonPath("$.body[0].tripMemberId").value(1))
                .andExpect(jsonPath("$.body[0].username").value("JustinBo123"))
                .andExpect(jsonPath("$.body[0].role").value("OWNER"))
                .andExpect(jsonPath("$.body[1].tripMemberId").value(2))
                .andExpect(jsonPath("$.body[1].username").value("FriendUser"))
                .andExpect(jsonPath("$.body[1].role").value("EDITOR"));

        verify(tripMemberService).getTripMembers(1L);
    }

    @Test
    void addTripMember_shouldReturnServiceResponse() throws Exception {
        AddTripMemberDTO request = new AddTripMemberDTO();
        request.setUsername("FriendUser");
        request.setRole("EDITOR");

        ResponseBody<Object> responseBody = new ResponseBody<>(
                "E000",
                "Trip member added successfully",
                TRIP_MEMBER.name(),
                Map.of(
                        "tripMemberId", 2,
                        "username", "FriendUser",
                        "role", "EDITOR"
                )
        );

        when(tripMemberService.addTripMember(eq(1L), any(AddTripMemberDTO.class)))
                .thenReturn(new CompleteResponse<>(responseBody, 200));

        mockMvc.perform(post("/api/v1/trips/1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("E000"))
                .andExpect(jsonPath("$.message").value("Trip member added successfully"))
                .andExpect(jsonPath("$.flow").value(TRIP_MEMBER.name()))
                .andExpect(jsonPath("$.body.tripMemberId").value(2))
                .andExpect(jsonPath("$.body.username").value("FriendUser"))
                .andExpect(jsonPath("$.body.role").value("EDITOR"));

        verify(tripMemberService).addTripMember(eq(1L), any(AddTripMemberDTO.class));
    }

    @Test
    void addTripMember_whenServiceReturnsForbidden_shouldReturnForbiddenStatus() throws Exception {
        AddTripMemberDTO request = new AddTripMemberDTO();
        request.setUsername("FriendUser");
        request.setRole("EDITOR");

        ResponseBody<Object> responseBody = new ResponseBody<>(
                "E064",
                "You do not have permission to access this trip",
                TRIP_MEMBER.name(),
                null
        );

        when(tripMemberService.addTripMember(eq(1L), any(AddTripMemberDTO.class)))
                .thenReturn(new CompleteResponse<>(responseBody, 403));

        mockMvc.perform(post("/api/v1/trips/1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("E064"))
                .andExpect(jsonPath("$.message").value("You do not have permission to access this trip"))
                .andExpect(jsonPath("$.flow").value(TRIP_MEMBER.name()));

        verify(tripMemberService).addTripMember(eq(1L), any(AddTripMemberDTO.class));
    }

    @Test
    void updateTripMemberRole_shouldReturnServiceResponse() throws Exception {
        UpdateTripMemberRoleDTO request = new UpdateTripMemberRoleDTO();
        request.setRole("VIEWER");

        ResponseBody<Object> responseBody = new ResponseBody<>(
                "E000",
                "Trip member role updated successfully",
                TRIP_MEMBER.name(),
                Map.of(
                        "tripMemberId", 2,
                        "username", "FriendUser",
                        "role", "VIEWER"
                )
        );

        when(tripMemberService.updateTripMemberRole(
                eq(1L),
                eq(2L),
                any(UpdateTripMemberRoleDTO.class)
        )).thenReturn(new CompleteResponse<>(responseBody, 200));

        mockMvc.perform(patch("/api/v1/trips/1/members/2/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("E000"))
                .andExpect(jsonPath("$.message").value("Trip member role updated successfully"))
                .andExpect(jsonPath("$.flow").value(TRIP_MEMBER.name()))
                .andExpect(jsonPath("$.body.tripMemberId").value(2))
                .andExpect(jsonPath("$.body.username").value("FriendUser"))
                .andExpect(jsonPath("$.body.role").value("VIEWER"));

        verify(tripMemberService).updateTripMemberRole(
                eq(1L),
                eq(2L),
                any(UpdateTripMemberRoleDTO.class)
        );
    }

    @Test
    void removeTripMember_shouldReturnServiceResponse() throws Exception {
        ResponseBody<Object> responseBody = new ResponseBody<>(
                "E000",
                "Trip member removed successfully",
                TRIP_MEMBER.name(),
                null
        );

        when(tripMemberService.removeTripMember(1L, 2L))
                .thenReturn(new CompleteResponse<>(responseBody, 200));

        mockMvc.perform(delete("/api/v1/trips/1/members/2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("E000"))
                .andExpect(jsonPath("$.message").value("Trip member removed successfully"))
                .andExpect(jsonPath("$.flow").value(TRIP_MEMBER.name()));

        verify(tripMemberService).removeTripMember(1L, 2L);
    }

    @Test
    void removeTripMember_whenServiceReturnsBadRequest_shouldReturnBadRequestStatus() throws Exception {
        ResponseBody<Object> responseBody = new ResponseBody<>(
                "E067",
                "Trip owner cannot be removed from the trip",
                TRIP_MEMBER.name(),
                null
        );

        when(tripMemberService.removeTripMember(1L, 1L))
                .thenReturn(new CompleteResponse<>(responseBody, 400));

        mockMvc.perform(delete("/api/v1/trips/1/members/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("E067"))
                .andExpect(jsonPath("$.message").value("Trip owner cannot be removed from the trip"))
                .andExpect(jsonPath("$.flow").value(TRIP_MEMBER.name()));

        verify(tripMemberService).removeTripMember(1L, 1L);
    }
}