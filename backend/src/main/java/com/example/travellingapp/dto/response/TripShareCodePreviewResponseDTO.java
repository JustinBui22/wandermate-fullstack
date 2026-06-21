package com.example.travellingapp.dto.response;

import com.example.travellingapp.enums.TripEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TripShareCodePreviewResponseDTO {
    private Long tripId;
    private String tripName;
    private String destination;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String ownerUsername;
    private TripEnum defaultRole;
    private LocalDateTime expiresAt;

    public TripShareCodePreviewResponseDTO() {
    }

    public TripShareCodePreviewResponseDTO(
            Long tripId,
            String tripName,
            String destination,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String ownerUsername,
            TripEnum defaultRole,
            LocalDateTime expiresAt
    ) {
        this.tripId = tripId;
        this.tripName = tripName;
        this.destination = destination;
        this.startDate = startDate;
        this.endDate = endDate;
        this.ownerUsername = ownerUsername;
        this.defaultRole = defaultRole;
        this.expiresAt = expiresAt;
    }
}
