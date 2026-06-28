package com.example.travellingapp.controller.impl;

import com.example.travellingapp.controller.TripCollaborationController;
import com.example.travellingapp.dto.request.SendTripInvitationDTO;
import com.example.travellingapp.dto.request.SendTripJoinRequestDTO;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.response_template.ResponseBody;
import com.example.travellingapp.service.TripCollaborationRequestService;
import com.example.travellingapp.service.TripOverlapWarningService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TripCollaborationControllerImpl implements TripCollaborationController {

    private final TripCollaborationRequestService tripCollaborationRequestService;
    private final TripOverlapWarningService tripOverlapWarningService;

    public TripCollaborationControllerImpl(TripCollaborationRequestService tripCollaborationRequestService, TripOverlapWarningService tripOverlapWarningService) {
        this.tripCollaborationRequestService = tripCollaborationRequestService;
        this.tripOverlapWarningService = tripOverlapWarningService;
    }

    @Override
    public ResponseEntity<ResponseBody<Object>> sendInvitation(Long tripId, SendTripInvitationDTO request) {
        CompleteResponse<Object> response = tripCollaborationRequestService.sendInvitation(tripId, request);
        return new ResponseEntity<>(response.getResponseBody(), HttpStatus.valueOf(response.getHttpCode()));
    }

    @Override
    public ResponseEntity<ResponseBody<Object>> getMyPendingInvitations() {
        CompleteResponse<Object> response = tripCollaborationRequestService.getMyPendingInvitations();
        return new ResponseEntity<>(response.getResponseBody(), HttpStatus.valueOf(response.getHttpCode()));
    }

    @Override
    public ResponseEntity<ResponseBody<Object>> acceptInvitation(Long requestId) {
        CompleteResponse<Object> response = tripCollaborationRequestService.acceptInvitation(requestId);
        return new ResponseEntity<>(response.getResponseBody(), HttpStatus.valueOf(response.getHttpCode()));
    }

    @Override
    public ResponseEntity<ResponseBody<Object>> rejectInvitation(Long requestId) {
        CompleteResponse<Object> response = tripCollaborationRequestService.rejectInvitation(requestId);
        return new ResponseEntity<>(response.getResponseBody(), HttpStatus.valueOf(response.getHttpCode()));
    }

    @Override
    public ResponseEntity<ResponseBody<Object>> requestToJoinTrip(Long tripId, SendTripJoinRequestDTO request) {
        CompleteResponse<Object> response = tripCollaborationRequestService.requestToJoinTrip(tripId, request);
        return new ResponseEntity<>(response.getResponseBody(), HttpStatus.valueOf(response.getHttpCode()));
    }

    @Override
    public ResponseEntity<ResponseBody<Object>> getPendingJoinRequests(Long tripId) {
        CompleteResponse<Object> response = tripCollaborationRequestService.getPendingJoinRequests(tripId);
        return new ResponseEntity<>(response.getResponseBody(), HttpStatus.valueOf(response.getHttpCode()));
    }

    @Override
    public ResponseEntity<ResponseBody<Object>> acceptJoinRequest(Long requestId) {
        CompleteResponse<Object> response = tripCollaborationRequestService.acceptJoinRequest(requestId);
        return new ResponseEntity<>(response.getResponseBody(), HttpStatus.valueOf(response.getHttpCode()));
    }

    @Override
    public ResponseEntity<ResponseBody<Object>> rejectJoinRequest(Long requestId) {
        CompleteResponse<Object> response = tripCollaborationRequestService.rejectJoinRequest(requestId);
        return new ResponseEntity<>(response.getResponseBody(), HttpStatus.valueOf(response.getHttpCode()));
    }

    @Override
    public ResponseEntity<ResponseBody<Object>> getOverlapWarnings(Long tripId) {
        CompleteResponse<Object> response = tripOverlapWarningService.getOverlapWarnings(tripId);
        return new ResponseEntity<>(response.getResponseBody(), HttpStatus.valueOf(response.getHttpCode()));
    }
}
