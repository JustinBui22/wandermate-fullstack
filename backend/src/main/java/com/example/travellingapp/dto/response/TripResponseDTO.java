package com.example.travellingapp.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TripResponseDTO {

    private Long tripId;
    private String tripName;
    private String destination;
    private LocalDateTime createdDate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime modifiedDate;

    private Long userId;
    private String username;

    public TripResponseDTO() {
    }

    public TripResponseDTO(Long tripId, String tripName, String destination,
                           LocalDateTime createdDate, LocalDateTime startDate,
                           LocalDateTime endDate, LocalDateTime modifiedDate,
                           Long userId, String username) {
        this.tripId = tripId;
        this.tripName = tripName;
        this.destination = destination;
        this.createdDate = createdDate;
        this.startDate = startDate;
        this.endDate = endDate;
        this.modifiedDate = modifiedDate;
        this.userId = userId;
        this.username = username;
    }
}

