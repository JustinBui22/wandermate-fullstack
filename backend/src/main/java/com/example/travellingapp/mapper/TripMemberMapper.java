package com.example.travellingapp.mapper;

import com.example.travellingapp.dto.response.TripMemberResponseDTO;
import com.example.travellingapp.entity.collaboration.TripMemberEntity;
import org.springframework.stereotype.Component;

@Component
public class TripMemberMapper {

    public TripMemberResponseDTO toResponseDTO(TripMemberEntity member) {
        TripMemberResponseDTO dto = new TripMemberResponseDTO();

        dto.setTripMemberId(member.getTripMemberId());
        dto.setRole(member.getRole());
        dto.setCreatedDate(member.getCreatedDate());
        dto.setModifiedDate(member.getModifiedDate());
        if (member.getTrip() != null) {
            dto.setTripId(member.getTrip().getTripId());
        }
        if (member.getUser() != null) {
            dto.setUserId(member.getUser().getUserId());
            dto.setUsername(member.getUser().getUsername());
            dto.setEmail(member.getUser().getEmail());
        }
        return dto;
    }
}
