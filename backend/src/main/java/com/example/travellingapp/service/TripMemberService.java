package com.example.travellingapp.service;

import com.example.travellingapp.dto.request.AddTripMemberDTO;
import com.example.travellingapp.dto.request.update.UpdateTripMemberRoleDTO;
import com.example.travellingapp.response_template.CompleteResponse;

public interface TripMemberService {

    CompleteResponse<Object> getTripMembers(Long tripId);

    CompleteResponse<Object> addTripMember(Long tripId, AddTripMemberDTO addTripMemberDTO);

    CompleteResponse<Object> updateTripMemberRole(
            Long tripId,
            Long tripMemberId,
            UpdateTripMemberRoleDTO updateTripMemberRoleDTO
    );

    CompleteResponse<Object> removeTripMember(Long tripId, Long tripMemberId);
}
