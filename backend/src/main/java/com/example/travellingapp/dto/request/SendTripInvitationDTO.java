package com.example.travellingapp.dto.request;

import com.example.travellingapp.enums.TripCollaborationEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendTripInvitationDTO {
    private String username;
    private TripCollaborationEnum role;
}
