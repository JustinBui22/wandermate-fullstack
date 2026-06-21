package com.example.travellingapp.dto.response;

import com.example.travellingapp.enums.TripEnum;
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

    private TripEnum currentUserRole;

    private TripEnum tripStatus;

    public TripResponseDTO() {
    }

    public TripResponseDTO(Long tripId, String tripName, String destination,
                           LocalDateTime createdDate, LocalDateTime startDate,
                           LocalDateTime endDate, LocalDateTime modifiedDate,
                           Long userId, String username, TripEnum tripStatus, TripEnum currentUserRole) {
        this.tripId = tripId;
        this.tripName = tripName;
        this.destination = destination;
        this.createdDate = createdDate;
        this.startDate = startDate;
        this.endDate = endDate;
        this.modifiedDate = modifiedDate;
        this.userId = userId;
        this.username = username;
        this.tripStatus = tripStatus;
        this.currentUserRole = currentUserRole;
    }

    public TripResponseDTO(Long tripId, String tripName, String destination,
                           LocalDateTime createdDate, LocalDateTime startDate,
                           LocalDateTime endDate, LocalDateTime modifiedDate,
                           Long userId, String username, TripEnum tripStatus) {
        this.tripId = tripId;
        this.tripName = tripName;
        this.destination = destination;
        this.createdDate = createdDate;
        this.startDate = startDate;
        this.endDate = endDate;
        this.modifiedDate = modifiedDate;
        this.userId = userId;
        this.username = username;
        this.tripStatus = tripStatus;
    }
}

