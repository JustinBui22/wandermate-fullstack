package com.example.travellingapp.service.impl;

import com.example.travellingapp.dto.request.create.CreateDestinationDTO;
import com.example.travellingapp.dto.request.update.UpdateDestinationDTO;
import com.example.travellingapp.dto.response.DestinationResponseDTO;
import com.example.travellingapp.entity.DestinationEntity;
import com.example.travellingapp.entity.TripEntity;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.mapper.DestinationMapper;
import com.example.travellingapp.repository.ActivityRepository;
import com.example.travellingapp.repository.DestinationRepository;
import com.example.travellingapp.repository.ErrorCodeRepository;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.security.data_security.AuthenticatedUserProvider;
import com.example.travellingapp.service.DestinationService;
import com.example.travellingapp.service.TripAccessService;
import com.example.travellingapp.validator.DestinationValidator;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.travellingapp.enums.CommonEnum.*;
import static com.example.travellingapp.enums.ErrorCodeEnum.*;
import static com.example.travellingapp.response_template.CompleteResponse.getCompleteResponse;

@Service
@Log4j2
public class DestinationServiceImpl implements DestinationService {

    private final DestinationRepository destinationRepository;
    private final ErrorCodeRepository errorCodeRepository;
    private final DestinationValidator destinationValidator;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final DestinationMapper destinationMapper;
    private final ActivityRepository activityRepository;
    private final TripAccessService tripAccessService;

    public DestinationServiceImpl(
            DestinationRepository destinationRepository,
            ErrorCodeRepository errorCodeRepository,
            DestinationValidator destinationValidator,
            AuthenticatedUserProvider authenticatedUserProvider,
            DestinationMapper destinationMapper,
            ActivityRepository activityRepository,
            TripAccessService tripAccessService) {
        this.destinationRepository = destinationRepository;
        this.errorCodeRepository = errorCodeRepository;
        this.destinationValidator = destinationValidator;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.destinationMapper = destinationMapper;
        this.activityRepository = activityRepository;
        this.tripAccessService = tripAccessService;
    }

    @Override
    public CompleteResponse<Object> createDestination(Long tripId, CreateDestinationDTO destinationDTO) {
        try {
            log.info("Creating destination for tripId: {}", tripId);

            // Validate input and get normalized destination name
            String destinationName = destinationValidator.validateCreateInput(
                    tripId,
                    destinationDTO
            );

            String username = authenticatedUserProvider.getUsername();

            // OWNER and EDITOR can create destination
            TripEntity trip = tripAccessService.getTripIfCanEdit(tripId, username);

            // Keep destination inside trip date range
            destinationValidator.validateDestinationInsideTrip(
                    destinationDTO.getStartDate(),
                    destinationDTO.getEndDate(),
                    trip
            );

            // Check for overlapping destinations
            boolean allowOverlap = Boolean.TRUE.equals(destinationDTO.getAllowOverlap());
            boolean hasOverlap = destinationRepository
                    .existsByTrip_TripIdAndStartDateLessThanAndEndDateGreaterThan(
                            tripId,
                            destinationDTO.getEndDate(),
                            destinationDTO.getStartDate()
                    );

            if (hasOverlap && !allowOverlap) {
                log.error("Destination date range overlaps with another destination in trip {}.", tripId);
                throw new BusinessException(DESTINATION_OVERLAP_WARNING, DESTINATION.name());
            }

            // Create destination
            DestinationEntity destination = new DestinationEntity(
                    destinationName,
                    destinationDTO.getStartDate(),
                    destinationDTO.getEndDate(),
                    destinationDTO.getDestinationOrder(),
                    destinationDTO.getNotes(),
                    LocalDateTime.now(),
                    null,
                    trip
            );

            destinationRepository.save(destination);
            return getCompleteResponse(
                    errorCodeRepository,
                    DESTINATION_CREATED_SUCCESS,
                    DESTINATION.name(),
                    destinationMapper.toResponseDTO(destination)
            );
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while creating destination", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }

    @Override
    public CompleteResponse<Object> getDestinationsByTrip(Long tripId) {
        try {
            log.info("Getting destinations for tripId: {}", tripId);
            if (tripId == null) {
                log.error("Invalid input to get destination list!");
                throw new BusinessException(INVALID_INPUT, COMMON.name());
            }

            String username = authenticatedUserProvider.getUsername();

            // OWNER, EDITOR and VIEWER can view destinations
            tripAccessService.assertCanView(tripId, username);

            // Get destinations ordered by destination order
            List<DestinationResponseDTO> destinations = destinationRepository
                    .findByTrip_TripIdOrderByDestinationOrderAsc(tripId)
                    .stream()
                    .map(destinationMapper::toResponseDTO)
                    .toList();

            return getCompleteResponse(
                    errorCodeRepository,
                    DESTINATION_RETRIEVED_SUCCESS,
                    DESTINATION.name(),
                    destinations
            );
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while getting destinations", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }

    @Override
    public CompleteResponse<Object> getDestinationById(Long tripId, Long destinationId) {
        try {
            log.info("Getting destination for tripId: {}, destinationId: {}", tripId, destinationId);
            if (tripId == null || destinationId == null) {
                log.error("Trip ID or Destination ID is missing to get a destination!");
                throw new BusinessException(INVALID_INPUT, COMMON.name());
            }

            String username = authenticatedUserProvider.getUsername();

            // OWNER, EDITOR and VIEWER can view destination
            tripAccessService.assertCanView(tripId, username);

            // Get destination by trip ID and destination ID
            DestinationEntity destination = destinationRepository
                    .findByDestinationIdAndTrip_TripId(destinationId, tripId)
                    .orElseThrow(() -> new BusinessException(DESTINATION_NOT_FOUND, DESTINATION.name()));

            return getCompleteResponse(
                    errorCodeRepository,
                    DESTINATION_RETRIEVED_SUCCESS,
                    DESTINATION.name(),
                    destinationMapper.toResponseDTO(destination)
            );

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while getting destination by ID", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }

    @Override
    public CompleteResponse<Object> updateDestination(Long tripId, Long destinationId, UpdateDestinationDTO destinationDTO) {
        try {
            log.info("Updating destination for tripId: {}, destinationId: {}", tripId, destinationId);

            // Validate input and get normalized destination name
            String destinationName = destinationValidator.validateUpdateInput(
                    tripId,
                    destinationId,
                    destinationDTO
            );

            String username = authenticatedUserProvider.getUsername();

            // OWNER and EDITOR can update destination
            TripEntity trip = tripAccessService.getTripIfCanEdit(tripId, username);

            // Get destination by trip ID and destination ID
            DestinationEntity destination = destinationRepository
                    .findByDestinationIdAndTrip_TripId(destinationId, tripId)
                    .orElseThrow(() -> new BusinessException(DESTINATION_NOT_FOUND, DESTINATION.name()));

            // Keep destination inside trip date range
            destinationValidator.validateDestinationInsideTrip(
                    destinationDTO.getStartDate(),
                    destinationDTO.getEndDate(),
                    trip
            );

            // Check if there are activities outside the updated destination date range
            boolean hasActivityOutsideUpdatedDestinationRange =
                    activityRepository.existsByDestination_DestinationIdAndStartDateTimeBefore(
                            destinationId,
                            destinationDTO.getStartDate()
                    )
                            || activityRepository.existsByDestination_DestinationIdAndEndDateTimeAfter(
                            destinationId,
                            destinationDTO.getEndDate()
                    );

            if (hasActivityOutsideUpdatedDestinationRange) {
                log.error("Updated destination date range does not include all existing activities in destination {}.", destinationId);
                throw new BusinessException(
                        DESTINATION_DATE_CONFLICT_WITH_EXISTING_ACTIVITY,
                        DESTINATION.name()
                );
            }

            // Check for overlapping destinations excluding the current one
            boolean allowOverlap = Boolean.TRUE.equals(destinationDTO.getAllowOverlap());
            boolean hasOverlap = destinationRepository
                    .existsByTrip_TripIdAndDestinationIdNotAndStartDateLessThanAndEndDateGreaterThan(
                            tripId,
                            destinationId,
                            destinationDTO.getEndDate(),
                            destinationDTO.getStartDate()
                    );

            if (hasOverlap && !allowOverlap) {
                log.error("Updated destination date range overlaps with another destination in trip {}.", tripId);
                throw new BusinessException(DESTINATION_OVERLAP_WARNING, DESTINATION.name());
            }

            // Update destination
            destination.setDestinationName(destinationName);
            destination.setStartDate(destinationDTO.getStartDate());
            destination.setEndDate(destinationDTO.getEndDate());
            destination.setDestinationOrder(destinationDTO.getDestinationOrder());
            destination.setNotes(destinationDTO.getNotes());
            destination.setModifiedDate(LocalDateTime.now());

            destinationRepository.save(destination);
            return getCompleteResponse(
                    errorCodeRepository,
                    DESTINATION_UPDATED_SUCCESS,
                    DESTINATION.name(),
                    destinationMapper.toResponseDTO(destination)
            );

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while updating destination", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }

    @Override
    public CompleteResponse<Object> deleteDestination(Long tripId, Long destinationId) {
        try {
            log.info("Deleting destination for tripId: {}, destinationId: {}", tripId, destinationId);

            if (tripId == null || destinationId == null) {
                log.error("Trip ID or Destination ID is missing to delete a destination!");
                throw new BusinessException(INVALID_INPUT, COMMON.name());
            }

            String username = authenticatedUserProvider.getUsername();

            // OWNER and EDITOR can delete destination
            tripAccessService.assertCanEdit(tripId, username);

            // Get destination by trip ID and destination ID
            DestinationEntity destination = destinationRepository
                    .findByDestinationIdAndTrip_TripId(destinationId, tripId)
                    .orElseThrow(() -> new BusinessException(DESTINATION_NOT_FOUND, DESTINATION.name()));

            // Delete destination
            destinationRepository.delete(destination);

            return getCompleteResponse(
                    errorCodeRepository,
                    DESTINATION_DELETED_SUCCESS,
                    DESTINATION.name(),
                    null
            );

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while deleting destination", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }
}