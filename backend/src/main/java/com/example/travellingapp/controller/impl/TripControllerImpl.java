package com.example.travellingapp.controller.impl;

import com.example.travellingapp.controller.TripController;
import com.example.travellingapp.dto.request.create.CreateTripDTO;
import com.example.travellingapp.dto.request.update.UpdateTripDTO;
import com.example.travellingapp.enums.TripEnum;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.response_template.ResponseBody;
import com.example.travellingapp.service.TripService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TripControllerImpl implements TripController {
    private final TripService tripService;

    public TripControllerImpl(TripService tripService) {
        this.tripService = tripService;
    }

    @Override
    public ResponseEntity<ResponseBody<Object>> searchCityList(String keyword) {
        CompleteResponse<Object> response = tripService.searchCityList(keyword);
        return new ResponseEntity<>(response.getResponseBody(), HttpStatus.valueOf(response.getHttpCode()));
    }

    @Override
    public ResponseEntity<ResponseBody<Object>> getTrips(TripEnum ownership, String status, TripEnum sort) {
        CompleteResponse<Object> response = tripService.getTrips(ownership, status, sort);
        return new ResponseEntity<>(
                response.getResponseBody(),
                HttpStatus.valueOf(response.getHttpCode())
        );
    }

    @Override
    public ResponseEntity<ResponseBody<Object>> getTripById(Long tripId) {
        CompleteResponse<Object> response = tripService.getTripById(tripId);
        return new ResponseEntity<>(
                response.getResponseBody(),
                HttpStatus.valueOf(response.getHttpCode())
        );
    }

    @Override
    public ResponseEntity<ResponseBody<Object>> updateTrip(Long tripId, UpdateTripDTO tripDTO) {
        CompleteResponse<Object> response = tripService.updateTrip(tripId, tripDTO);
        return new ResponseEntity<>(
                response.getResponseBody(),
                HttpStatus.valueOf(response.getHttpCode())
        );
    }

    @Override
    public ResponseEntity<ResponseBody<Object>> deleteTrip(Long tripId) {
        CompleteResponse<Object> response = tripService.deleteTrip(tripId);

        return new ResponseEntity<>(
                response.getResponseBody(),
                HttpStatus.valueOf(response.getHttpCode())
        );
    }

    @Override
    public ResponseEntity<ResponseBody<Object>> createTrip(CreateTripDTO tripDTO) {
        CompleteResponse<Object> response = tripService.createTrip(tripDTO);
        return new ResponseEntity<>(response.getResponseBody(), HttpStatus.valueOf(response.getHttpCode()));
    }

    @Override
    public ResponseEntity<ResponseBody<Object>> searchRestaurantList(String keyword) {
        CompleteResponse<Object> response = tripService.searchRestaurantList(keyword);
        return new ResponseEntity<>(response.getResponseBody(), HttpStatus.valueOf(response.getHttpCode()));
    }

    @Override
    public ResponseEntity<ResponseBody<Object>> searchAccommodationList(String keyword) {
        CompleteResponse<Object> response = tripService.searchAccommodationList(keyword);
        return new ResponseEntity<>(response.getResponseBody(), HttpStatus.valueOf(response.getHttpCode()));
    }

    @Override
    public ResponseEntity<ResponseBody<Object>> suggestCityList(String keyword) {
        CompleteResponse<Object> response = tripService.suggestCityList(keyword);
        return new ResponseEntity<>(response.getResponseBody(), HttpStatus.valueOf(response.getHttpCode()));
    }

    @Override
    public ResponseEntity<ResponseBody<Object>> suggestRestaurantList(String keyword) {
        CompleteResponse<Object> response = tripService.suggestRestaurantList(keyword);
        return new ResponseEntity<>(response.getResponseBody(), HttpStatus.valueOf(response.getHttpCode()));
    }

    @Override
    public ResponseEntity<ResponseBody<Object>> suggestAccommodationList(String keyword) {
        CompleteResponse<Object> response = tripService.suggestAccommodationList(keyword);
        return new ResponseEntity<>(response.getResponseBody(), HttpStatus.valueOf(response.getHttpCode()));
    }

}


