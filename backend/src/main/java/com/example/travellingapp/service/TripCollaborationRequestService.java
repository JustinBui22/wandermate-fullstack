package com.example.travellingapp.service;
import com.example.travellingapp.dto.request.SendTripInvitationDTO;
import com.example.travellingapp.dto.request.SendTripJoinRequestDTO;
import com.example.travellingapp.response_template.CompleteResponse;

public interface TripCollaborationRequestService {

    CompleteResponse<Object> sendInvitation(Long tripId, SendTripInvitationDTO request);

    CompleteResponse<Object> getMyPendingInvitations();

    CompleteResponse<Object> acceptInvitation(Long requestId);

    CompleteResponse<Object> rejectInvitation(Long requestId);

    CompleteResponse<Object> requestToJoinTrip(Long tripId, SendTripJoinRequestDTO request);

    CompleteResponse<Object> getPendingJoinRequests(Long tripId);

    CompleteResponse<Object> acceptJoinRequest(Long requestId);

    CompleteResponse<Object> rejectJoinRequest(Long requestId);
}
