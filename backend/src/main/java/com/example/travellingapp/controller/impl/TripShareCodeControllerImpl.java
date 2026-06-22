package com.example.travellingapp.controller.impl;

import com.example.travellingapp.controller.TripShareCodeController;
import com.example.travellingapp.dto.request.create.GenerateTripShareCodeRequest;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.response_template.ResponseBody;
import com.example.travellingapp.service.TripShareCodeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TripShareCodeControllerImpl implements TripShareCodeController {

    private final TripShareCodeService tripShareCodeService;

    public TripShareCodeControllerImpl(TripShareCodeService tripShareCodeService) {
        this.tripShareCodeService = tripShareCodeService;
    }

    @Override
    public ResponseEntity<ResponseBody<Object>> regenerateShareCode(
            Long tripId,
            GenerateTripShareCodeRequest request
    ) {
        CompleteResponse<Object> response =
                tripShareCodeService.regenerateShareCode(tripId, request);

        return new ResponseEntity<>(
                response.getResponseBody(),
                HttpStatus.valueOf(response.getHttpCode())
        );
    }

    @Override
    public ResponseEntity<ResponseBody<Object>> previewShareCode(String code) {
        CompleteResponse<Object> response =
                tripShareCodeService.previewShareCode(code);

        return new ResponseEntity<>(
                response.getResponseBody(),
                HttpStatus.valueOf(response.getHttpCode())
        );
    }

    @Override
    public ResponseEntity<ResponseBody<Object>> requestToJoinByShareCode(String code) {
        CompleteResponse<Object> response =
                tripShareCodeService.requestToJoinByShareCode(code);

        return new ResponseEntity<>(
                response.getResponseBody(),
                HttpStatus.valueOf(response.getHttpCode())
        );
    }

    @Override
    public ResponseEntity<ResponseBody<Object>> getActiveShareCode(Long tripId) {
        CompleteResponse<Object> response =
                tripShareCodeService.getActiveShareCode(tripId);

        return new ResponseEntity<>(
                response.getResponseBody(),
                HttpStatus.valueOf(response.getHttpCode())
        );
    }
}