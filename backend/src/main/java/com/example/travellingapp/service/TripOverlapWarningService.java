package com.example.travellingapp.service;

import com.example.travellingapp.dto.response.MyTripOverlapWarningDTO;
import com.example.travellingapp.entity.TripEntity;
import com.example.travellingapp.response_template.CompleteResponse;

import java.util.List;

public interface TripOverlapWarningService {

    CompleteResponse<Object> getOverlapWarnings(Long tripId);

    List<MyTripOverlapWarningDTO> buildWarningsForUser(
            TripEntity currentTrip,
            String username
    );
}
