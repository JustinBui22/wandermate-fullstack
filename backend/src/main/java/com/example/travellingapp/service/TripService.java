package com.example.travellingapp.service;

import com.example.travellingapp.dto.request.create.CreateTripDTO;
import com.example.travellingapp.dto.request.update.UpdateTripDTO;
import com.example.travellingapp.response_template.CompleteResponse;


public interface TripService {
    CompleteResponse<Object> createTrip(CreateTripDTO tripDTO);

    CompleteResponse<Object> getTrips();

    CompleteResponse<Object> getTripById(Long tripId);

    CompleteResponse<Object> updateTrip(Long tripId, UpdateTripDTO tripDTO);

    CompleteResponse<Object> deleteTrip(Long tripId);

    CompleteResponse<Object> suggestCityList(String keyword);

    CompleteResponse<Object> suggestRestaurantList(String keyword);

    CompleteResponse<Object> suggestAccommodationList(String keyword);

    CompleteResponse<Object> searchCityList(String keyword);

    CompleteResponse<Object> searchRestaurantList(String keyword);

    CompleteResponse<Object> searchAccommodationList(String keyword);
}
