package com.example.travellingapp.dto.response;

import com.example.travellingapp.enums.TripCollaborationEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TripCollaborationRequestResponseDTO {
    private Long requestId;

    private Long tripId;
    private String tripName;
    private String destination;
    private LocalDateTime tripStartDate;
    private LocalDateTime tripEndDate;

    private Long requesterUserId;
    private String requesterUsername;

    private Long targetUserId;
    private String targetUsername;

    private TripCollaborationEnum requestedRole;
    private TripCollaborationEnum requestType;
    private TripCollaborationEnum status;

    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private LocalDateTime respondedDate;
}
