package com.example.travellingapp.service.impl;

import com.example.travellingapp.dto.response.MyTripOverlapWarningDTO;
import com.example.travellingapp.entity.TripEntity;
import com.example.travellingapp.enums.TripEnum;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.mapper.TripOverlapWarningMapper;
import com.example.travellingapp.repository.ErrorCodeRepository;
import com.example.travellingapp.repository.collaboration.TripMemberRepository;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.security.data_security.AuthenticatedUserProvider;
import com.example.travellingapp.service.TripAccessService;
import com.example.travellingapp.service.TripOverlapWarningService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.travellingapp.enums.CommonEnum.COMMON;
import static com.example.travellingapp.enums.CommonEnum.TRIP_MEMBER;
import static com.example.travellingapp.enums.ErrorCodeEnum.*;
import static com.example.travellingapp.response_template.CompleteResponse.getCompleteResponse;

@Service
@Log4j2
public class TripOverlapWarningServiceImpl implements TripOverlapWarningService {

    private final TripMemberRepository tripMemberRepository;
    private final ErrorCodeRepository errorCodeRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final TripAccessService tripAccessService;
    private final TripOverlapWarningMapper tripOverlapWarningMapper;

    public TripOverlapWarningServiceImpl(
            TripMemberRepository tripMemberRepository,
            ErrorCodeRepository errorCodeRepository,
            AuthenticatedUserProvider authenticatedUserProvider,
            TripAccessService tripAccessService, TripOverlapWarningMapper tripOverlapWarningMapper) {
        this.tripMemberRepository = tripMemberRepository;
        this.errorCodeRepository = errorCodeRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.tripAccessService = tripAccessService;
        this.tripOverlapWarningMapper = tripOverlapWarningMapper;
    }

    @Override
    public CompleteResponse<Object> getOverlapWarnings(Long tripId) {
        try {
            // Validate trip ID
            if (tripId == null) {
                log.error("Trip ID is null");
                throw new BusinessException(INVALID_INPUT, COMMON.name());
            }

            // Get current logged-in user
            String username = authenticatedUserProvider.getUsername();

            // Check if current user can view this trip and get trip detail
            TripEntity currentTrip = tripAccessService.getTripIfCanView(tripId, username);

            // Get current user's role in this trip
            TripEnum role = tripAccessService.getUserRole(tripId, username);

            // Owner should not receive overlap warning from this endpoint
            if (role == TripEnum.OWNER) {
                return getCompleteResponse(
                        errorCodeRepository,
                        TRIP_OVERLAP_WARNINGS_RETRIEVED_SUCCESS,
                        TRIP_MEMBER.name(),
                        List.of()
                );
            }

            // Build private overlap warning for current member only
            List<MyTripOverlapWarningDTO> warnings = buildWarningsForUser(currentTrip, username);

            return getCompleteResponse(
                    errorCodeRepository,
                    TRIP_OVERLAP_WARNINGS_RETRIEVED_SUCCESS,
                    TRIP_MEMBER.name(),
                    warnings
            );
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while getting current user's trip overlap warnings", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }

    @Override
    public List<MyTripOverlapWarningDTO> buildWarningsForUser(TripEntity currentTrip, String username) {
        // Validate input before checking overlap
        if (currentTrip == null || currentTrip.getTripId() == null || username == null || username.isBlank()) {
            log.error("Invalid input for building warnings: currentTrip={}, username={}", currentTrip, username);
            throw new BusinessException(INVALID_INPUT, COMMON.name());
        }

        // Find other trips that overlap with this shared trip for the current user
        return tripMemberRepository.findOverlappingTripsForMember(
                        username,
                        currentTrip.getTripId(),
                        currentTrip.getStartDate(),
                        currentTrip.getEndDate()
                )
                .stream()
                .map(overlappingTrip ->  tripOverlapWarningMapper.toWarningDTO(currentTrip, overlappingTrip))
                .toList();
    }
}