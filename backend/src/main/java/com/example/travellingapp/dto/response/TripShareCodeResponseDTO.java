package com.example.travellingapp.dto.response;

import com.example.travellingapp.enums.TripEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TripShareCodeResponseDTO {
    private Long tripId;
    private String tripName;
    private String code;
    private String inviteLink;
    private TripEnum defaultRole;
    private TripEnum codeStatus;
    private LocalDateTime expiresAt;
    private LocalDateTime createdDate;

    public TripShareCodeResponseDTO() {
    }

    public TripShareCodeResponseDTO(
            Long tripId,
            String tripName,
            String code,
            String inviteLink,
            TripEnum defaultRole,
            TripEnum codeStatus,
            LocalDateTime expiresAt,
            LocalDateTime createdDate
    ) {
        this.tripId = tripId;
        this.tripName = tripName;
        this.code = code;
        this.inviteLink = inviteLink;
        this.defaultRole = defaultRole;
        this.codeStatus = codeStatus;
        this.expiresAt = expiresAt;
        this.createdDate = createdDate;
    }
}