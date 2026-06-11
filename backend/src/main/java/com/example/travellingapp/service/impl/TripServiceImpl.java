package com.example.travellingapp.service.impl;

import com.example.travellingapp.dto.request.create.CreateTripDTO;
import com.example.travellingapp.dto.request.update.UpdateTripDTO;
import com.example.travellingapp.dto.response.TripResponseDTO;
import com.example.travellingapp.entity.*;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.mapper.TripMapper;
import com.example.travellingapp.repository.*;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.security.data_security.AuthenticatedUserProvider;
import com.example.travellingapp.service.TripService;
import com.example.travellingapp.validator.TripValidator;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.travellingapp.enums.CommonEnum.*;
import static com.example.travellingapp.enums.ErrorCodeEnum.*;
import static com.example.travellingapp.response_template.CompleteResponse.getCompleteResponse;
import static com.example.travellingapp.util.Common.*;
import static com.example.travellingapp.util.DataConverter.convertStringToInt;

@Service
@Log4j2
public class TripServiceImpl implements TripService {
    private final ErrorCodeRepository errorCodeRepository;
    private final CityRepository cityRepository;
    private final RestaurantRepository restaurantRepository;
    private final AccommodationRepository accommodationRepository;
    private final ConfigurationRepository configurationRepository;
    private final UserRepository userRepository;
    private final TripRepository tripRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final TripMapper tripMapper;
    private final TripValidator tripValidator;
    private final DestinationRepository destinationRepository;

    public TripServiceImpl(ErrorCodeRepository errorCodeRepository, CityRepository cityRepository, RestaurantRepository restaurantRepository, AccommodationRepository accommodationRepository, ConfigurationRepository configurationRepository, UserRepository userRepository, TripRepository tripRepository, AuthenticatedUserProvider authenticatedUserProvider, TripMapper tripMapper, TripValidator tripValidator, DestinationRepository destinationRepository) {
        this.errorCodeRepository = errorCodeRepository;
        this.cityRepository = cityRepository;
        this.restaurantRepository = restaurantRepository;
        this.accommodationRepository = accommodationRepository;
        this.configurationRepository = configurationRepository;
        this.userRepository = userRepository;
        this.tripRepository = tripRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.tripMapper = tripMapper;
        this.tripValidator = tripValidator;
        this.destinationRepository = destinationRepository;
    }

    @Override
    public CompleteResponse<Object> createTrip(CreateTripDTO tripDTO) {
        try {
            String tripName = tripValidator.validateCreateInput(tripDTO);
            String destination = normalizeKeyword(tripDTO.getDestination());
            String username = authenticatedUserProvider.getUsername();

            User user = userRepository.findByUsernameAndActive(username, true)
                    .orElseThrow(() -> new BusinessException(USER_NOT_FOUND, COMMON.name()));

            // Validate trip name uniqueness for the user
            if (tripRepository.existsByUser_UsernameAndTripNameIgnoreCase(username, tripName)) {
                log.error("Trip name {} already exists for current user!", tripName);
                throw new BusinessException(TRIP_NAME_ALREADY_EXISTS, COMMON.name());
            }
            // Check for overlapping trips if allowOverlap is not true
            boolean allowOverlap = Boolean.TRUE.equals(tripDTO.getAllowOverlap());
            boolean hasOverlap = tripRepository
                    .existsByUser_UsernameAndStartDateLessThanAndEndDateGreaterThan(
                            username,
                            tripDTO.getEndDate(),
                            tripDTO.getStartDate()
                    );

            if (hasOverlap && !allowOverlap) {
                log.error("Created trip date range overlaps with another existing trip for user {}.", username);
                throw new BusinessException(TRIP_OVERLAP_WARNING, TRIP.name());
            }

            TripEntity trip = new TripEntity(tripName, destination, LocalDateTime.now(), tripDTO.getStartDate(), tripDTO.getEndDate(), null, user);
            tripRepository.save(trip);
            return getCompleteResponse(errorCodeRepository, TRIP_CREATED_SUCCESS, TRIP.name(), tripMapper.toResponseDTO(trip));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while creating trip", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }

    @Override
    public CompleteResponse<Object> getTrips() {
        try {
            log.info("Getting trips for user {}", authenticatedUserProvider.getUsername());
            String username = authenticatedUserProvider.getUsername();
            User user = userRepository.findByUsernameAndActive(username, true)
                    .orElseThrow(() -> new BusinessException(USER_NOT_FOUND, COMMON.name()));
            List<TripResponseDTO> trips = tripRepository.findAllByUser(user)
                    .stream()
                    .map(tripMapper::toResponseDTO)
                    .toList();
            return getCompleteResponse(
                    errorCodeRepository,
                    TRIPS_RETRIEVED_SUCCESS,
                    TRIP.name(),
                    trips
            );

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while getting trips", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }

    @Override
    public CompleteResponse<Object> getTripById(Long tripId) {
        try {
            if (tripId == null) {
                log.error("Trip ID is missing!");
                throw new BusinessException(INVALID_INPUT, COMMON.name());
            }
            String username = authenticatedUserProvider.getUsername();
            TripEntity trip = tripRepository.findByTripIdAndUser_Username(tripId, username)
                    .orElseThrow(() -> new BusinessException(TRIP_NOT_FOUND, COMMON.name()));

            return getCompleteResponse(
                    errorCodeRepository,
                    TRIPS_RETRIEVED_SUCCESS,
                    TRIP.name(),
                    tripMapper.toResponseDTO(trip)
            );

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while getting trip by ID", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }

    @Override
    public CompleteResponse<Object> updateTrip(Long tripId, UpdateTripDTO tripDTO) {
        try {
            String tripName = tripValidator.validateUpdateInput(tripId, tripDTO);
            String destination = normalizeKeyword(tripDTO.getDestination());
            String username = authenticatedUserProvider.getUsername();
            TripEntity trip = tripRepository
                    .findByTripIdAndUser_Username(tripId, username)
                    .orElseThrow(() -> new BusinessException(INVALID_INPUT, COMMON.name()));
            // Validate trip name uniqueness for the user excluding the current trip
            if (tripRepository.existsByUser_UsernameAndTripNameIgnoreCaseAndTripIdNot(
                    username,
                    tripName,
                    tripId
            )) {
                log.error("Trip name {} already exists for user {}!", tripName, username);
                throw new BusinessException(TRIP_NAME_ALREADY_EXISTS, COMMON.name());
            }

            // Check for overlapping trips if allowOverlap is not true
            boolean allowOverlap = Boolean.TRUE.equals(tripDTO.getAllowOverlap());
            boolean hasOverlap = tripRepository
                    .existsByUser_UsernameAndTripIdNotAndStartDateLessThanAndEndDateGreaterThan(
                            username,
                            tripId,
                            tripDTO.getEndDate(),
                            tripDTO.getStartDate()
                    );
            if (hasOverlap && !allowOverlap) {
                log.error("Updated trip date range overlaps with another existing trip for user {}.", username);
                throw new BusinessException(TRIP_OVERLAP_WARNING, TRIP.name());
            }

            // Check if there are any destinations that would fall outside the new trip date range
            boolean hasDestinationOutsideNewTripRange =
                    destinationRepository.existsByTrip_TripIdAndStartDateBefore(
                            tripId,
                            tripDTO.getStartDate()
                    )
                            || destinationRepository.existsByTrip_TripIdAndEndDateAfter(
                            tripId,
                            tripDTO.getEndDate()
                    );
            if (hasDestinationOutsideNewTripRange) {
                log.error("Updated trip date range does not cover all destinations in trip {}.", tripId);
                throw new BusinessException(TRIP_DATE_CONFLICT_WITH_EXISTING_DESTINATION, TRIP.name());
            }

            trip.setTripName(tripName);
            trip.setDestination(destination);
            trip.setStartDate(tripDTO.getStartDate());
            trip.setEndDate(tripDTO.getEndDate());
            trip.setModifiedDate(LocalDateTime.now());

            tripRepository.save(trip);
            return getCompleteResponse(
                    errorCodeRepository,
                    TRIP_UPDATED_SUCCESS,
                    TRIP.name(),
                    tripMapper.toResponseDTO(trip)
            );
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while updating trip", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }

    @Override
    public CompleteResponse<Object> deleteTrip(Long tripId) {
        try {
            if (tripId == null) {
                log.error("Trip ID is missing to execute delete trip!");
                throw new BusinessException(INVALID_INPUT, COMMON.name());
            }
            String username = authenticatedUserProvider.getUsername();
            TripEntity trip = tripRepository
                    .findByTripIdAndUser_Username(tripId, username)
                    .orElseThrow(() -> new BusinessException(TRIP_NOT_FOUND, TRIP.name()));

            tripRepository.delete(trip);

            return getCompleteResponse(
                    errorCodeRepository,
                    TRIP_DELETED_SUCCESS,
                    TRIP.name(),
                    null
            );

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while deleting trip", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }

    @Override
    public CompleteResponse<Object> suggestCityList(String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);
        if (isInvalidSuggestKeyword(normalizedKeyword)) {
            return emptySearchResponse();
        }
        List<CityEntity> cityList = cityRepository.findTop10ByCityNameStartingWithIgnoreCaseOrderByCityNameAsc(normalizedKeyword);
        if (cityList.isEmpty()) {
            log.info("No city suggestion found as {}", normalizedKeyword);
        }
        return getCompleteResponse(errorCodeRepository, SEARCH_INFO_SUCCESS, COMMON.name(), cityList);
    }

    @Override
    public CompleteResponse<Object> suggestRestaurantList(String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);
        if (isInvalidSuggestKeyword(normalizedKeyword)) {
            return emptySearchResponse();
        }
        List<RestaurantEntity> restaurantList =
                restaurantRepository.findTop10ByRestaurantNameStartingWithIgnoreCaseOrderByRestaurantNameAsc(normalizedKeyword);
        if (restaurantList.isEmpty()) {
            log.info("No restaurant suggestion found as {}", normalizedKeyword);
        }
        return getCompleteResponse(errorCodeRepository, SEARCH_INFO_SUCCESS, COMMON.name(), restaurantList);
    }

    @Override
    public CompleteResponse<Object> suggestAccommodationList(String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);
        if (isInvalidSuggestKeyword(normalizedKeyword)) {
            return emptySearchResponse();
        }
        List<AccommodationEntity> accommodationList =
                accommodationRepository.findTop10ByAccommodationNameStartingWithIgnoreCaseOrderByAccommodationNameAsc(normalizedKeyword);
        if (accommodationList.isEmpty()) {
            log.info("No accommodation suggestion found as {}", normalizedKeyword);
        }
        return getCompleteResponse(errorCodeRepository, SEARCH_INFO_SUCCESS, COMMON.name(), accommodationList);
    }

    @Override
    public CompleteResponse<Object> searchCityList(String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);
        if (tripValidator.isBlankKeyword(normalizedKeyword)) {
            return emptySearchResponse();
        }
        List<CityEntity> cityList = cityRepository.findAllByCityNameContainingIgnoreCase(normalizedKeyword);
        if (cityList.isEmpty()) {
            log.info("No city found as {}", normalizedKeyword);
        }
        return getCompleteResponse(errorCodeRepository, SEARCH_INFO_SUCCESS, COMMON.name(), cityList);
    }

    @Override
    public CompleteResponse<Object> searchRestaurantList(String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);
        if (tripValidator.isBlankKeyword(normalizedKeyword)) {
            return emptySearchResponse();
        }
        List<RestaurantEntity> restaurantList = restaurantRepository.findAllByRestaurantNameContainingIgnoreCase(normalizedKeyword);
        if (restaurantList.isEmpty()) {
            log.info("No restaurant found as {}", normalizedKeyword);
        }
        return getCompleteResponse(errorCodeRepository, SEARCH_INFO_SUCCESS, COMMON.name(), restaurantList);
    }

    @Override
    public CompleteResponse<Object> searchAccommodationList(String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);
        if (tripValidator.isBlankKeyword(normalizedKeyword)) {
            return emptySearchResponse();
        }
        List<AccommodationEntity> accommodationList = accommodationRepository.findAllByAccommodationNameContainingIgnoreCase(normalizedKeyword);
        if (accommodationList.isEmpty()) {
            log.info("No accommodation found as {}", normalizedKeyword);
        }
        return getCompleteResponse(errorCodeRepository, SEARCH_INFO_SUCCESS, COMMON.name(), accommodationList);
    }

    private boolean isInvalidSuggestKeyword(String keyword) {
        return keyword.length() < getMinSuggestCharacter();
    }

    private int getMinSuggestCharacter() {
        return convertStringToInt(
                getConfigValue(MIN_SUGGEST_CHARACTER.name(), configurationRepository, "2")
        );
    }

    private CompleteResponse<Object> emptySearchResponse() {
        return getCompleteResponse(errorCodeRepository, SEARCH_INFO_SUCCESS, COMMON.name(), List.of());
    }
}