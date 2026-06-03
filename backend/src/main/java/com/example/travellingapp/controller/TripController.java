package com.example.travellingapp.controller;

import com.example.travellingapp.dto.request.create.CreateTripDTO;
import com.example.travellingapp.dto.request.update.UpdateTripDTO;
import com.example.travellingapp.response_template.ResponseBody;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/trips")
public interface TripController {
    @PostMapping
    ResponseEntity<ResponseBody<Object>> createTrip(@Valid @RequestBody CreateTripDTO tripDTO);

    @GetMapping("/search/cities")
    ResponseEntity<ResponseBody<Object>> searchCityList(@NotNull @RequestParam(name = "keyword") String keyword);

    @GetMapping
    ResponseEntity<ResponseBody<Object>> getTrips();

    @GetMapping("/{tripId}")
    ResponseEntity<ResponseBody<Object>> getTripById(
            @PathVariable Long tripId
    );

    @PutMapping("/{tripId}")
    ResponseEntity<ResponseBody<Object>> updateTrip(
            @PathVariable Long tripId,
            @Valid @RequestBody UpdateTripDTO tripDTO
    );

    @DeleteMapping("/{tripId}")
    ResponseEntity<ResponseBody<Object>> deleteTrip(
            @NotNull @PathVariable Long tripId
    );

    @GetMapping("/search/restaurants")
    ResponseEntity<ResponseBody<Object>> searchRestaurantList(@NotNull @RequestParam(name = "keyword") String keyword);

    @GetMapping("/search/accommodations")
    ResponseEntity<ResponseBody<Object>> searchAccommodationList(@NotNull @RequestParam(name = "keyword") String keyword);

    @GetMapping("/suggest/cities")
    ResponseEntity<ResponseBody<Object>> suggestCityList(@NotNull @RequestParam(name = "keyword") String keyword);

    @GetMapping("/suggest/restaurants")
    ResponseEntity<ResponseBody<Object>> suggestRestaurantList(@NotNull @RequestParam(name = "keyword") String keyword);

    @GetMapping("/suggest/accommodations")
    ResponseEntity<ResponseBody<Object>> suggestAccommodationList(@NotNull @RequestParam(name = "keyword") String keyword);
}
