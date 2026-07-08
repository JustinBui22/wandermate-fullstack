package com.example.travellingapp.service.impl;

import com.example.travellingapp.dto.request.create.CreateTripDTO;
import com.example.travellingapp.dto.request.update.UpdateTripDTO;
import com.example.travellingapp.dto.response.TripResponseDTO;
import com.example.travellingapp.entity.*;
import com.example.travellingapp.entity.collaboration.TripMemberEntity;
import com.example.travellingapp.enums.TripEnum;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.mapper.TripMapper;
import com.example.travellingapp.repository.*;
import com.example.travellingapp.repository.collaboration.TripMemberRepository;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.security.data_security.AuthenticatedUserProvider;
import com.example.travellingapp.service.CloudinaryImageClient;
import com.example.travellingapp.service.TripAccessService;
import com.example.travellingapp.service.TripService;
import com.example.travellingapp.validator.TripValidator;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

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
    private final TripMemberRepository tripMemberRepository;
    private final TripAccessService tripAccessService;
    private final CloudinaryImageClient cloudinaryImageClient;

    public TripServiceImpl(
            ErrorCodeRepository errorCodeRepository,
            CityRepository cityRepository,
            RestaurantRepository restaurantRepository,
            AccommodationRepository accommodationRepository,
            ConfigurationRepository configurationRepository,
            UserRepository userRepository,
            TripRepository tripRepository,
            AuthenticatedUserProvider authenticatedUserProvider,
            TripMapper tripMapper,
            TripValidator tripValidator,
            DestinationRepository destinationRepository,
            TripMemberRepository tripMemberRepository,
            TripAccessService tripAccessService, CloudinaryImageClient cloudinaryImageClient) {
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
        this.tripMemberRepository = tripMemberRepository;
        this.tripAccessService = tripAccessService;
        this.cloudinaryImageClient = cloudinaryImageClient;
    }

    @Override
    public CompleteResponse<Object> createTrip(CreateTripDTO tripDTO) {
        try {
            // Validate trip input and normalize trip data
            String tripName = tripValidator.validateCreateInput(tripDTO);
            String destination = normalizeKeyword(tripDTO.getDestination());
            String username = authenticatedUserProvider.getUsername();

            // Check if authenticated user exists and is active
            User user = userRepository.findByUsernameAndActive(username)
                    .orElseThrow(() -> new BusinessException(USER_NOT_FOUND, COMMON.name()));

            // Check if trip name already exists for this user
            if (tripRepository.existsByUser_UsernameAndTripNameIgnoreCase(username, tripName)) {
                log.error("Trip name {} already exists for current user!", tripName);
                throw new BusinessException(TRIP_NAME_ALREADY_EXISTS, COMMON.name());
            }

            // Check if the new trip overlaps with the user's existing trips
            boolean allowOverlap = Boolean.TRUE.equals(tripDTO.getAllowOverlap());
            boolean hasOverlap = tripRepository
                    .existsByUser_UsernameAndStartDateLessThanAndEndDateGreaterThan(
                            username,
                            tripDTO.getEndDate(),
                            tripDTO.getStartDate()
                    );

            // Block overlapping trip unless the user explicitly allows overlap
            if (hasOverlap && !allowOverlap) {
                log.error("Created trip date range overlaps with another existing trip for user {}.", username);
                throw new BusinessException(TRIP_OVERLAP_WARNING, TRIP.name());
            }

            // Create the trip entity
            TripEntity trip = new TripEntity(
                    tripName,
                    destination,
                    LocalDateTime.now(),
                    tripDTO.getStartDate(),
                    tripDTO.getEndDate(),
                    null,
                    user
            );

            // Allow user to set status manually
            if (tripDTO.getTripStatus() != null) {
                trip.setTripStatus(tripDTO.getTripStatus());
            }

            // Autocorrect status based on trip date
            refreshTripStatusIfNeeded(trip);

            // Set cover image URL and public ID if provided
            trip.setCoverImageUrl(trimToNull(tripDTO.getCoverImageUrl()));
            trip.setCoverImagePublicId(trimToNull(tripDTO.getCoverImagePublicId()));

            // Save trip first so it has tripId for member relation
            TripEntity savedTrip = tripRepository.save(trip);

            // Automatically add creator as OWNER member
            TripMemberEntity ownerMember = new TripMemberEntity(
                    savedTrip,
                    user,
                    TripEnum.OWNER,
                    LocalDateTime.now()
            );
            tripMemberRepository.save(ownerMember);

            // Build response with current user's role and trip status
            TripResponseDTO responseDTO = tripMapper.toResponseDTO(savedTrip);
            responseDTO.setTripStatus(savedTrip.getTripStatus());
            responseDTO.setCurrentUserRole(TripEnum.OWNER);

            return getCompleteResponse(
                    errorCodeRepository,
                    TRIP_CREATED_SUCCESS,
                    TRIP.name(),
                    responseDTO
            );
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while creating trip", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }

    @Override
    public CompleteResponse<Object> getTrips(TripEnum ownership, String status, TripEnum sort) {
        try {
            log.info("Getting trips for user {}", authenticatedUserProvider.getUsername());

            String username = authenticatedUserProvider.getUsername();

            // Check if authenticated user exists and is active
            userRepository.findByUsernameAndActive(username)
                    .orElseThrow(() -> new BusinessException(USER_NOT_FOUND, COMMON.name()));

            // Validate and resolve trip filters
            TripEnum ownershipFilter = tripValidator.validateOwnershipFilter(ownership);
            TripEnum statusFilter = tripValidator.validateStatusFilter(status);
            TripEnum sortOption = tripValidator.validateSortOption(sort);

            // Fetch all trips where current user is OWNER, EDITOR or VIEWER
            List<TripEntity> trips = tripMemberRepository.findAccessibleTripsByUsername(username);

            // Refresh live trip status before returning data
            for (TripEntity trip : trips) {
                refreshTripStatusIfNeeded(trip);
            }
            tripRepository.saveAll(trips);

            // Convert trips to DTOs and attach current user's role
            List<TripResponseDTO> tripResponses = trips.stream()
                    .map(trip -> toTripResponseDTOWithCurrentUserRole(trip, username))
                    .toList();

            // Apply ownership filter, status filter and sort option
            List<TripResponseDTO> filteredTrips = applyTripFiltersAndSort(
                    tripResponses,
                    ownershipFilter,
                    statusFilter,
                    sortOption
            );
            return getCompleteResponse(
                    errorCodeRepository,
                    TRIPS_RETRIEVED_SUCCESS,
                    TRIP.name(),
                    filteredTrips
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
            // Check if trip ID is missing
            if (tripId == null) {
                log.error("Trip ID is missing!");
                throw new BusinessException(INVALID_INPUT, COMMON.name());
            }
            String username = authenticatedUserProvider.getUsername();

            // Check if current user can view this trip
            TripEntity trip = tripAccessService.getTripIfCanView(tripId, username);

            // Refresh live trip status before returning trip details
            refreshTripStatusIfNeeded(trip);
            tripRepository.save(trip);

            // Build response with current user's role and trip status
            TripResponseDTO responseDTO = toTripResponseDTOWithCurrentUserRole(trip, username);

            return getCompleteResponse(
                    errorCodeRepository,
                    TRIPS_RETRIEVED_SUCCESS,
                    TRIP.name(),
                    responseDTO
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
            // Validate trip input and normalize trip data
            String tripName = tripValidator.validateUpdateInput(tripId, tripDTO);
            String destination = normalizeKeyword(tripDTO.getDestination());
            String username = authenticatedUserProvider.getUsername();

            // Check if current user can edit this trip
            TripEntity trip = tripAccessService.getTripIfCanEdit(tripId, username);

            // Use trip owner's username for duplicate and overlap checks
            String tripOwnerUsername = trip.getUser().getUsername();

            // Check if another trip owned by the owner already has this name
            if (tripRepository.existsByUser_UsernameAndTripNameIgnoreCaseAndTripIdNot(
                    tripOwnerUsername,
                    tripName,
                    tripId)) {
                log.error("Trip name {} already exists for user {}!", tripName, tripOwnerUsername);
                throw new BusinessException(TRIP_NAME_ALREADY_EXISTS, COMMON.name());
            }

            // Check if updated date range overlaps with the owner's other trips
            boolean allowOverlap = Boolean.TRUE.equals(tripDTO.getAllowOverlap());
            boolean hasOverlap = tripRepository
                    .existsByUser_UsernameAndTripIdNotAndStartDateLessThanAndEndDateGreaterThan(
                            tripOwnerUsername,
                            tripId,
                            tripDTO.getEndDate(),
                            tripDTO.getStartDate()
                    );

            // Block overlapping trip unless user explicitly allows overlap
            if (hasOverlap && !allowOverlap) {
                log.error("Updated trip date range overlaps with another existing trip for trip owner {}.", tripOwnerUsername);
                throw new BusinessException(TRIP_OVERLAP_WARNING, TRIP.name());
            }

            // Check if existing destinations would fall outside the new trip date range
            boolean hasDestinationOutsideNewTripRange =
                    destinationRepository.existsByTrip_TripIdAndStartDateBefore(
                            tripId,
                            tripDTO.getStartDate()
                    )
                            || destinationRepository.existsByTrip_TripIdAndEndDateAfter(
                            tripId,
                            tripDTO.getEndDate()
                    );

            // Block update if the new trip date does not include all existing destinations
            if (hasDestinationOutsideNewTripRange) {
                log.error("Updated trip date range does not cover all destinations in trip {}.", tripId);
                throw new BusinessException(TRIP_DATE_CONFLICT_WITH_EXISTING_DESTINATION, TRIP.name());
            }

            // Update basic trip fields
            trip.setTripName(tripName);
            trip.setDestination(destination);
            trip.setStartDate(tripDTO.getStartDate());
            trip.setEndDate(tripDTO.getEndDate());
            trip.setModifiedDate(LocalDateTime.now());

            // Update cover image URL if provided
            String oldCoverImagePublicId = trip.getCoverImagePublicId();
            trip.setCoverImageUrl(trimToNull(tripDTO.getCoverImageUrl()));
            trip.setCoverImagePublicId(trimToNull(tripDTO.getCoverImagePublicId()));


            // Allow user to set status manually
            if (tripDTO.getTripStatus() != null) {
                trip.setTripStatus(tripDTO.getTripStatus());
            }

            // Autocorrect status based on trip date
            refreshTripStatusIfNeeded(trip);

            // Save updated trip
            tripRepository.save(trip);

            // Delete old cover image from Cloudinary if it has changed
            cloudinaryImageClient.deleteOldCloudinaryImageIfChanged(
                    oldCoverImagePublicId,
                    trip.getCoverImagePublicId(),
                    "trip cover"
            );

            // Build response with current user's role and trip status
            TripResponseDTO responseDTO = toTripResponseDTOWithCurrentUserRole(trip, username);

            return getCompleteResponse(
                    errorCodeRepository,
                    TRIP_UPDATED_SUCCESS,
                    TRIP.name(),
                    responseDTO
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
            // Check if trip ID is missing
            if (tripId == null) {
                log.error("Trip ID is missing to execute delete trip!");
                throw new BusinessException(INVALID_INPUT, COMMON.name());
            }
            String username = authenticatedUserProvider.getUsername();

            // Only owner can delete trip
            TripEntity trip = tripAccessService.getTripIfOwner(tripId, username);

            String oldCoverImagePublicId = trip.getCoverImagePublicId();

            // Delete trip
            tripRepository.delete(trip);

            // Delete old cover image from Cloudinary if it exists
            cloudinaryImageClient.deleteOldCloudinaryImageIfChanged(oldCoverImagePublicId, null, "trip cover");;

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
        // Normalize keyword
        String normalizedKeyword = normalizeKeyword(keyword);

        // Return empty list if keyword is too short
        if (isInvalidSuggestKeyword(normalizedKeyword)) {
            return emptySearchResponse();
        }

        // Search city suggestions by prefix
        List<CityEntity> cityList =
                cityRepository.findTop10ByCityNameStartingWithIgnoreCaseOrderByCityNameAsc(
                        normalizedKeyword
                );

        if (cityList.isEmpty()) {
            log.info("No city suggestion found as {}", normalizedKeyword);
        }
        return getCompleteResponse(
                errorCodeRepository,
                SEARCH_INFO_SUCCESS,
                COMMON.name(),
                cityList
        );
    }

    @Override
    public CompleteResponse<Object> suggestRestaurantList(String keyword) {
        // Normalize keyword
        String normalizedKeyword = normalizeKeyword(keyword);

        // Return empty list if keyword is too short
        if (isInvalidSuggestKeyword(normalizedKeyword)) {
            return emptySearchResponse();
        }

        // Search restaurant suggestions by prefix
        List<RestaurantEntity> restaurantList =
                restaurantRepository.findTop10ByRestaurantNameStartingWithIgnoreCaseOrderByRestaurantNameAsc(
                        normalizedKeyword
                );

        if (restaurantList.isEmpty()) {
            log.info("No restaurant suggestion found as {}", normalizedKeyword);
        }

        return getCompleteResponse(
                errorCodeRepository,
                SEARCH_INFO_SUCCESS,
                COMMON.name(),
                restaurantList
        );
    }

    @Override
    public CompleteResponse<Object> suggestAccommodationList(String keyword) {
        // Normalize keyword
        String normalizedKeyword = normalizeKeyword(keyword);

        // Return empty list if keyword is too short
        if (isInvalidSuggestKeyword(normalizedKeyword)) {
            return emptySearchResponse();
        }

        // Search accommodation suggestions by prefix
        List<AccommodationEntity> accommodationList =
                accommodationRepository.findTop10ByAccommodationNameStartingWithIgnoreCaseOrderByAccommodationNameAsc(
                        normalizedKeyword
                );

        if (accommodationList.isEmpty()) {
            log.info("No accommodation suggestion found as {}", normalizedKeyword);
        }

        return getCompleteResponse(
                errorCodeRepository,
                SEARCH_INFO_SUCCESS,
                COMMON.name(),
                accommodationList
        );
    }

    @Override
    public CompleteResponse<Object> searchCityList(String keyword) {
        // Normalize keyword
        String normalizedKeyword = normalizeKeyword(keyword);

        // Return empty list if keyword is blank
        if (tripValidator.isBlankKeyword(normalizedKeyword)) {
            return emptySearchResponse();
        }

        // Search cities by containing keyword
        List<CityEntity> cityList =
                cityRepository.findAllByCityNameContainingIgnoreCase(normalizedKeyword);

        if (cityList.isEmpty()) {
            log.info("No city found as {}", normalizedKeyword);
        }

        return getCompleteResponse(
                errorCodeRepository,
                SEARCH_INFO_SUCCESS,
                COMMON.name(),
                cityList
        );
    }

    @Override
    public CompleteResponse<Object> searchRestaurantList(String keyword) {
        // Normalize keyword
        String normalizedKeyword = normalizeKeyword(keyword);

        // Return empty list if keyword is blank
        if (tripValidator.isBlankKeyword(normalizedKeyword)) {
            return emptySearchResponse();
        }

        // Search restaurants by containing keyword
        List<RestaurantEntity> restaurantList =
                restaurantRepository.findAllByRestaurantNameContainingIgnoreCase(normalizedKeyword);

        if (restaurantList.isEmpty()) {
            log.info("No restaurant found as {}", normalizedKeyword);
        }

        return getCompleteResponse(
                errorCodeRepository,
                SEARCH_INFO_SUCCESS,
                COMMON.name(),
                restaurantList
        );
    }

    @Override
    public CompleteResponse<Object> searchAccommodationList(String keyword) {
        // Normalize keyword
        String normalizedKeyword = normalizeKeyword(keyword);

        // Return empty list if keyword is blank
        if (tripValidator.isBlankKeyword(normalizedKeyword)) {
            return emptySearchResponse();
        }

        // Search accommodations by containing keyword
        List<AccommodationEntity> accommodationList =
                accommodationRepository.findAllByAccommodationNameContainingIgnoreCase(normalizedKeyword);

        if (accommodationList.isEmpty()) {
            log.info("No accommodation found as {}", normalizedKeyword);
        }

        return getCompleteResponse(
                errorCodeRepository,
                SEARCH_INFO_SUCCESS,
                COMMON.name(),
                accommodationList
        );
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
        return getCompleteResponse(
                errorCodeRepository,
                SEARCH_INFO_SUCCESS,
                COMMON.name(),
                List.of()
        );
    }

    private TripEnum resolveTripStatus(TripEntity trip) {
        LocalDateTime now = LocalDateTime.now();

        // If trip end date has passed, mark as finished
        if (trip.getEndDate() != null && trip.getEndDate().isBefore(now)) {
            return TripEnum.FINISHED;
        }

        // If current date is within trip date range, mark as ongoing
        if (trip.getStartDate() != null
                && trip.getEndDate() != null
                && !now.isBefore(trip.getStartDate())
                && !now.isAfter(trip.getEndDate())) {
            return TripEnum.ONGOING;
        }

        // Otherwise, the trip is still in planning
        return TripEnum.PLANNING;
    }

    private void refreshTripStatusIfNeeded(TripEntity trip) {
        // Resolve latest status from current date and trip date range
        TripEnum resolvedStatus = resolveTripStatus(trip);

        // Update trip status only if it changed
        if (trip.getTripStatus() != resolvedStatus) {
            trip.setTripStatus(resolvedStatus);
            trip.setModifiedDate(LocalDateTime.now());
        }
    }

    private List<TripResponseDTO> applyTripFiltersAndSort(
            List<TripResponseDTO> trips,
            TripEnum ownership,
            TripEnum status,
            TripEnum sort
    ) {
        Stream<TripResponseDTO> stream = trips.stream();

        // Filter trips created by current user
        if (ownership == TripEnum.CREATED) {
            stream = stream.filter(trip ->
                    trip.getCurrentUserRole() == TripEnum.OWNER
            );
        }

        // Filter trips joined by current user
        if (ownership == TripEnum.JOINED) {
            stream = stream.filter(trip ->
                    trip.getCurrentUserRole() == TripEnum.EDITOR
                            || trip.getCurrentUserRole() == TripEnum.VIEWER
            );
        }

        // Filter trips by status
        if (status != null && !status.name().equalsIgnoreCase(TripEnum.ALL.name())) {
            TripEnum statusEnum;

            try {
                statusEnum = TripEnum.valueOf(status.name());
            } catch (IllegalArgumentException e) {
                throw new BusinessException(TRIP_STATUS_INVALID, TRIP.name());
            }

            if (statusEnum.getGroup() != TripEnum.Group.STATUS) {
                throw new BusinessException(TRIP_STATUS_INVALID, TRIP.name());
            }

            stream = stream.filter(trip -> trip.getTripStatus() == statusEnum);
        }

        // Sort trips
        Comparator<TripResponseDTO> comparator = getTripComparator(sort);

        return stream
                .sorted(comparator)
                .toList();
    }

    private Comparator<TripResponseDTO> getTripComparator(TripEnum sort) {
        // Sort by name A-Z
        if (sort == TripEnum.NAME_ASC) {
            return Comparator.comparing(
                    trip -> trip.getTripName() == null
                            ? ""
                            : trip.getTripName().toLowerCase()
            );
        }

        // Sort by name Z-A
        if (sort == TripEnum.NAME_DESC) {
            return Comparator
                    .comparing((TripResponseDTO trip) ->
                            trip.getTripName() == null
                                    ? ""
                                    : trip.getTripName().toLowerCase()
                    )
                    .reversed();
        }

        // Sort by created date oldest first
        if (sort == TripEnum.CREATED_DATE_ASC) {
            return Comparator.comparing(
                    TripResponseDTO::getCreatedDate,
                    Comparator.nullsLast(Comparator.naturalOrder())
            );
        }

        // Sort by created date newest first
        if (sort == TripEnum.CREATED_DATE_DESC) {
            return Comparator
                    .comparing(
                            TripResponseDTO::getCreatedDate,
                            Comparator.nullsLast(Comparator.naturalOrder())
                    )
                    .reversed();
        }

        // Sort by modified date oldest first
        if (sort == TripEnum.MODIFIED_DATE_ASC) {
            return Comparator.comparing(
                    this::getTripUpdatedSortValue,
                    Comparator.nullsLast(Comparator.naturalOrder())
            );
        }

        // Default sort by modified date newest first
        return Comparator
                .comparing(
                        this::getTripUpdatedSortValue,
                        Comparator.nullsLast(Comparator.naturalOrder())
                )
                .reversed();
    }

    private LocalDateTime getTripUpdatedSortValue(TripResponseDTO trip) {
        // Use modified date if available
        if (trip.getModifiedDate() != null) {
            return trip.getModifiedDate();
        }

        // Fall back to created date
        return trip.getCreatedDate();
    }

    private TripResponseDTO toTripResponseDTOWithCurrentUserRole(
            TripEntity trip,
            String username
    ) {
        // Map normal trip fields
        TripResponseDTO dto = tripMapper.toResponseDTO(trip);

        // Add resolved trip status
        dto.setTripStatus(trip.getTripStatus());

        // Add current user's role for frontend permission checks
        tripMemberRepository
                .findByTrip_TripIdAndUser_Username(trip.getTripId(), username)
                .ifPresent(member -> dto.setCurrentUserRole(member.getRole()));

        return dto;
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isBlank()) {
            return null;
        }
        return value.trim();
    }
}