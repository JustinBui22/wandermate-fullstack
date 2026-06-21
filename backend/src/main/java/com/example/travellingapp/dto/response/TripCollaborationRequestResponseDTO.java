package com.example.travellingapp.dto.response;

import com.example.travellingapp.enums.TripEnum;
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

    private TripEnum requestedRole;
    private TripEnum requestType;
    private TripEnum status;

    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private LocalDateTime respondedDate;
}
