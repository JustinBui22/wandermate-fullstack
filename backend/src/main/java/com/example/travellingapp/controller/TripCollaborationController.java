package com.example.travellingapp.controller;

import com.example.travellingapp.dto.request.SendTripInvitationDTO;
import com.example.travellingapp.dto.request.SendTripJoinRequestDTO;
import com.example.travellingapp.response_template.ResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/trips")
public interface TripCollaborationController {

    @PostMapping("/{tripId}/invitations")
    ResponseEntity<ResponseBody<Object>> sendInvitation(
            @PathVariable Long tripId,
            @RequestBody SendTripInvitationDTO request
    );

    @GetMapping("/invitations/received")
    ResponseEntity<ResponseBody<Object>> getMyPendingInvitations();

    @PatchMapping("/invitations/{requestId}/accept")
    ResponseEntity<ResponseBody<Object>> acceptInvitation(
            @PathVariable Long requestId
    );

    @PatchMapping("/invitations/{requestId}/reject")
    ResponseEntity<ResponseBody<Object>> rejectInvitation(
            @PathVariable Long requestId
    );

    @PostMapping("/{tripId}/join-requests")
    ResponseEntity<ResponseBody<Object>> requestToJoinTrip(
            @PathVariable Long tripId,
            @RequestBody SendTripJoinRequestDTO request
    );

    @GetMapping("/{tripId}/join-requests")
    ResponseEntity<ResponseBody<Object>> getPendingJoinRequests(
            @PathVariable Long tripId
    );

    @PatchMapping("/join-requests/{requestId}/accept")
    ResponseEntity<ResponseBody<Object>> acceptJoinRequest(
            @PathVariable Long requestId
    );

    @PatchMapping("/join-requests/{requestId}/reject")
    ResponseEntity<ResponseBody<Object>> rejectJoinRequest(
            @PathVariable Long requestId
    );

    @GetMapping("/{tripId}/my-overlap-warnings")
    ResponseEntity<ResponseBody<Object>> getOverlapWarnings(
            @PathVariable Long tripId
    );
}