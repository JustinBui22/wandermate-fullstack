package com.example.travellingapp.service.impl;

import com.example.travellingapp.entity.TripEntity;
import com.example.travellingapp.enums.TripMemberRoleEnum;
import com.example.travellingapp.entity.collaboration.TripMemberEntity;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.repository.TripRepository;
import com.example.travellingapp.repository.collaboration.TripMemberRepository;
import com.example.travellingapp.service.TripAccessService;
import org.springframework.stereotype.Service;

import static com.example.travellingapp.enums.CommonEnum.COMMON;
import static com.example.travellingapp.enums.ErrorCodeEnum.*;

@Service
public class TripAccessServiceImpl implements TripAccessService {
    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;

    public TripAccessServiceImpl(
            TripRepository tripRepository,
            TripMemberRepository tripMemberRepository
    ) {
        this.tripRepository = tripRepository;
        this.tripMemberRepository = tripMemberRepository;
    }

    @Override
    public TripMemberRoleEnum getUserRole(Long tripId, String username) {
        return tripMemberRepository
                .findByTrip_TripIdAndUser_UsernameAndUser_IsActiveTrue(tripId, username)
                .map(TripMemberEntity::getRole)
                .orElseThrow(() -> new BusinessException(TRIP_ACCESS_DENIED, COMMON.name()));
    }

    @Override
    public TripEntity getTripIfCanView(Long tripId, String username) {
        assertCanView(tripId, username);

        return tripRepository.findById(tripId)
                .orElseThrow(() -> new BusinessException(TRIP_NOT_FOUND, COMMON.name()));
    }

    @Override
    public TripEntity getTripIfCanEdit(Long tripId, String username) {
        assertCanEdit(tripId, username);

        return tripRepository.findById(tripId)
                .orElseThrow(() -> new BusinessException(TRIP_NOT_FOUND, COMMON.name()));
    }

    @Override
    public TripEntity getTripIfOwner(Long tripId, String username) {
        assertIsOwner(tripId, username);
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new BusinessException(TRIP_NOT_FOUND, COMMON.name()));
    }

    @Override
    public void assertCanView(Long tripId, String username) {
        if (tripId == null || username == null || username.isBlank()) {
            throw new BusinessException(INVALID_INPUT, COMMON.name());
        }
        boolean hasAccess = tripMemberRepository
                .existsByTrip_TripIdAndUser_UsernameAndUser_IsActiveTrue(tripId, username);
        if (!hasAccess) {
            throw new BusinessException(TRIP_ACCESS_DENIED, COMMON.name());
        }
    }

    @Override
    public void assertCanEdit(Long tripId, String username) {
        TripMemberRoleEnum role = getUserRole(tripId, username);
        if (role != TripMemberRoleEnum.OWNER && role != TripMemberRoleEnum.EDITOR) {
            throw new BusinessException(TRIP_ACCESS_DENIED, COMMON.name());
        }
    }

    @Override
    public void assertIsOwner(Long tripId, String username) {
        TripMemberRoleEnum role = getUserRole(tripId, username);
        if (role != TripMemberRoleEnum.OWNER) {
            throw new BusinessException(TRIP_ACCESS_DENIED, COMMON.name());
        }
    }
}