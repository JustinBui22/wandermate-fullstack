package com.example.travellingapp.repository.collaboration.projection;

public interface TripPendingJoinRequestCountProjection {
    Long getTripId();

    Long getPendingCount();
}
