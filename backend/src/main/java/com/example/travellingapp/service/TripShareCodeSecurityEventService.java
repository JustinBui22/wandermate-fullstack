package com.example.travellingapp.service;

public interface TripShareCodeSecurityEventService {
    void recordInvalidAttempt(long userId);

    void recordExpiredCodeAndInvalidAttempt(Long shareCodeId, long userId);
}
