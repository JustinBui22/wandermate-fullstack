package com.example.travellingapp.service;

import com.example.travellingapp.entity.TripEntity;
import com.example.travellingapp.enums.TripMemberRoleEnum;

public interface TripAccessService {

    TripMemberRoleEnum getUserRole(Long tripId, String username);

    TripEntity getTripIfCanView(Long tripId, String username);

    TripEntity getTripIfCanEdit(Long tripId, String username);

    TripEntity getTripIfOwner(Long tripId, String username);

    void assertCanView(Long tripId, String username);

    void assertCanEdit(Long tripId, String username);

    void assertIsOwner(Long tripId, String username);
}
