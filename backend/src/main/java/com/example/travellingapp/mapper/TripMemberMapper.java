package com.example.travellingapp.mapper;

import com.example.travellingapp.dto.response.TripMemberResponseDTO;
import com.example.travellingapp.entity.collaboration.TripMemberEntity;
import org.springframework.stereotype.Component;

@Component
public class TripMemberMapper {

    public TripMemberResponseDTO toResponseDTO(TripMemberEntity tripMember) {
        return new TripMemberResponseDTO(
                tripMember.getTripMemberId(),
                tripMember.getTrip().getTripId(),
                tripMember.getUser().getUserId(),
                tripMember.getUser().getUsername(),
                tripMember.getUser().getEmail(),
                tripMember.getRole(),
                tripMember.getCreatedDate(),
                tripMember.getModifiedDate()
        );
    }
}
