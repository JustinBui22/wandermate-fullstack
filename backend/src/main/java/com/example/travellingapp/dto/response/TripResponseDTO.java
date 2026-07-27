package com.example.travellingapp.dto.response;

import com.example.travellingapp.enums.TripEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
public class TripResponseDTO {

    private Long tripId;
    private String tripName;
    private String destination;
    private Instant createdDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private Instant modifiedDate;

    private Long userId;
    private String username;

    private TripEnum currentUserRole;

    private TripEnum tripStatus;

    private String coverImageUrl;
    private String coverImagePublicId;

    public TripResponseDTO() {
    }

    public TripResponseDTO(Long tripId, String tripName, String destination,
                           Instant createdDate, LocalDate startDate,
                           LocalDate endDate, Instant modifiedDate,
                           Long userId, String username, String coverImageUrl, String coverImagePublicId, TripEnum tripStatus, TripEnum currentUserRole) {
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
        this.coverImageUrl = coverImageUrl;
        this.coverImagePublicId = coverImagePublicId;
    }

    public TripResponseDTO(Long tripId, String tripName, String destination,
                           Instant createdDate, LocalDate startDate,
                           LocalDate endDate, Instant modifiedDate,
                           Long userId, String username, String coverImageUrl, String coverImagePublicId, TripEnum tripStatus) {
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
        this.coverImageUrl = coverImageUrl;
        this.coverImagePublicId = coverImagePublicId;
    }
}
