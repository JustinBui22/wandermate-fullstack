package com.example.travellingapp.mapper;

import com.example.travellingapp.dto.response.TripShareCodePreviewResponseDTO;
import com.example.travellingapp.dto.response.TripShareCodeResponseDTO;
import com.example.travellingapp.entity.TripEntity;
import com.example.travellingapp.entity.User;
import com.example.travellingapp.entity.collaboration.TripCollaborationRequestEntity;
import com.example.travellingapp.entity.collaboration.TripShareCodeEntity;
import com.example.travellingapp.enums.TripEnum;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TripShareCodeMapper {

    private static final String INVITE_LINK_PREFIX = "wandermate://join-trip?code=";

    public TripShareCodeEntity toNewShareCodeEntity(
            TripEntity trip,
            String code,
            User owner,
            TripEnum defaultRole,
            LocalDateTime now,
            long expiryHours
    ) {
        return new TripShareCodeEntity(
                trip,
                code,
                owner,
                defaultRole,
                TripEnum.ACTIVE,
                now.plusHours(expiryHours),
                now
        );
    }

    public TripCollaborationRequestEntity toJoinRequestEntity(
            TripShareCodeEntity shareCode,
            User requester,
            LocalDateTime now
    ) {
        TripEntity trip = shareCode.getTrip();
        User owner = trip.getUser();

        return new TripCollaborationRequestEntity(
                trip,
                requester,
                owner,
                shareCode.getDefaultRole(),
                TripEnum.JOIN_REQUEST,
                TripEnum.PENDING,
                now
        );
    }

    public TripShareCodeResponseDTO toResponseDTO(TripShareCodeEntity shareCode) {
        return new TripShareCodeResponseDTO(
                shareCode.getTrip().getTripId(),
                shareCode.getTrip().getTripName(),
                shareCode.getCode(),
                buildInviteLink(shareCode.getCode()),
                shareCode.getDefaultRole(),
                shareCode.getCodeStatus(),
                shareCode.getExpiresAt(),
                shareCode.getCreatedDate()
        );
    }

    public TripShareCodePreviewResponseDTO toPreviewResponseDTO(TripShareCodeEntity shareCode) {
        TripEntity trip = shareCode.getTrip();

        return new TripShareCodePreviewResponseDTO(
                trip.getTripId(),
                trip.getTripName(),
                trip.getDestination(),
                trip.getStartDate(),
                trip.getEndDate(),
                trip.getUser().getUsername(),
                shareCode.getDefaultRole(),
                shareCode.getExpiresAt()
        );
    }

    private String buildInviteLink(String code) {
        return INVITE_LINK_PREFIX + code;
    }
}
