package com.example.travellingapp.dto.response;

import com.example.travellingapp.enums.TripEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
public class TripShareCodePreviewResponseDTO {
    private Long tripId;
    private String tripName;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private String ownerUsername;
    private TripEnum defaultRole;
    private Instant expiresAt;

    public TripShareCodePreviewResponseDTO() {
    }

    public TripShareCodePreviewResponseDTO(
            Long tripId,
            String tripName,
            String destination,
            LocalDate startDate,
            LocalDate endDate,
            String ownerUsername,
            TripEnum defaultRole,
            Instant expiresAt
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
