package com.example.travellingapp.service;

import com.example.travellingapp.dto.request.create.GenerateTripShareCodeRequest;
import com.example.travellingapp.response_template.CompleteResponse;

public interface TripShareCodeService {

    CompleteResponse<Object> regenerateShareCode(
            Long tripId,
            GenerateTripShareCodeRequest request
    );

    CompleteResponse<Object> previewShareCode(String code);

    CompleteResponse<Object> requestToJoinByShareCode(String code);

    CompleteResponse<Object> getActiveShareCode(Long tripId);
}