package com.example.travellingapp.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TripCollaborationActionResponseDTO {
    private TripCollaborationRequestResponseDTO request;
    private TripMemberResponseDTO member;
    private List<MyTripOverlapWarningDTO> overlapWarnings;

    public TripCollaborationActionResponseDTO(
            TripCollaborationRequestResponseDTO request,
            TripMemberResponseDTO member,
            List<MyTripOverlapWarningDTO> overlapWarnings
    ) {
        this.request = request;
        this.member = member;
        this.overlapWarnings = overlapWarnings;
    }
}
