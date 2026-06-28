package com.example.travellingapp.controller.impl;

import com.example.travellingapp.controller.TripMemberController;
import com.example.travellingapp.dto.request.AddTripMemberDTO;
import com.example.travellingapp.dto.request.update.UpdateTripMemberRoleDTO;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.response_template.ResponseBody;
import com.example.travellingapp.service.TripMemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class TripMemberControllerImpl implements TripMemberController {
    private final TripMemberService tripMemberService;

    public TripMemberControllerImpl(TripMemberService tripMemberService) {
        this.tripMemberService = tripMemberService;
    }

    public ResponseEntity<ResponseBody<Object>> getTripMembers(Long tripId) {
        CompleteResponse<Object> response = tripMemberService.getTripMembers(tripId);
        return new ResponseEntity<>(response.getResponseBody(), HttpStatus.valueOf(response.getHttpCode()));
    }

    public ResponseEntity<ResponseBody<Object>> updateTripMemberRole(Long tripId, Long tripMemberId, UpdateTripMemberRoleDTO updateTripMemberRoleDTO) {
        CompleteResponse<Object> response = tripMemberService.updateTripMemberRole(tripId, tripMemberId, updateTripMemberRoleDTO);
        return new ResponseEntity<>(response.getResponseBody(), HttpStatus.valueOf(response.getHttpCode()));
    }

    public ResponseEntity<ResponseBody<Object>> removeTripMember(Long tripId, Long tripMemberId) {
        CompleteResponse<Object> response = tripMemberService.removeTripMember(tripId, tripMemberId);
        return new ResponseEntity<>(response.getResponseBody(), HttpStatus.valueOf(response.getHttpCode()));
    }
}
