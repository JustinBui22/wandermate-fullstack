package com.example.travellingapp.dto.response;

import com.example.travellingapp.enums.TripEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
public class TripCollaborationRequestResponseDTO {
    private Long requestId;

    private Long tripId;
    private String tripName;
    private String destination;
    private LocalDate tripStartDate;
    private LocalDate tripEndDate;

    private Long requesterUserId;
    private String requesterUsername;

    private Long targetUserId;
    private String targetUsername;

    private TripEnum requestedRole;
    private TripEnum requestType;
    private TripEnum status;

    private Instant createdDate;
    private Instant modifiedDate;
    private Instant respondedDate;
}
