package com.example.travellingapp.service.impl;

import com.example.travellingapp.dto.request.create.CreateActivityDTO;
import com.example.travellingapp.dto.request.update.UpdateActivityDTO;
import com.example.travellingapp.dto.response.ActivityResponseDTO;
import com.example.travellingapp.entity.ActivityEntity;
import com.example.travellingapp.entity.DestinationEntity;
import com.example.travellingapp.entity.User;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.mapper.ActivityMapper;
import com.example.travellingapp.repository.ActivityRepository;
import com.example.travellingapp.repository.DestinationRepository;
import com.example.travellingapp.repository.ErrorCodeRepository;
import com.example.travellingapp.repository.UserRepository;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.security.data_security.AuthenticatedUserProvider;
import com.example.travellingapp.service.ActivityService;
import com.example.travellingapp.service.TripAccessService;
import com.example.travellingapp.validator.ActivityValidator;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.travellingapp.enums.CommonEnum.*;
import static com.example.travellingapp.enums.ErrorCodeEnum.*;
import static com.example.travellingapp.response_template.CompleteResponse.getCompleteResponse;
import static com.example.travellingapp.util.Common.normalizeKeyword;

@Service
@Log4j2
public class ActivityServiceImpl implements ActivityService {
    private final ActivityRepository activityRepository;
    private final DestinationRepository destinationRepository;
    private final ErrorCodeRepository errorCodeRepository;
    private final ActivityValidator activityValidator;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final ActivityMapper activityMapper;
    private final UserRepository userRepository;
    private final TripAccessService tripAccessService;

    public ActivityServiceImpl(
            ActivityRepository activityRepository,
            DestinationRepository destinationRepository,
            ErrorCodeRepository errorCodeRepository,
            ActivityValidator activityValidator,
            AuthenticatedUserProvider authenticatedUserProvider,
            ActivityMapper activityMapper,
            UserRepository userRepository,
            TripAccessService tripAccessService) {
        this.activityRepository = activityRepository;
        this.destinationRepository = destinationRepository;
        this.errorCodeRepository = errorCodeRepository;
        this.activityValidator = activityValidator;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.activityMapper = activityMapper;
        this.userRepository = userRepository;
        this.tripAccessService = tripAccessService;
    }

    @Override
    public CompleteResponse<Object> createActivity(Long tripId, Long destinationId, CreateActivityDTO activityDTO) {
        try {
            log.info("Creating activity for tripId: {}, destinationId: {}", tripId, destinationId);
            // Validate input and get normalized activity name
            String activityName = activityValidator.validateCreateInput(
                    tripId,
                    destinationId,
                    activityDTO
            );

            String username = authenticatedUserProvider.getUsername();

            // OWNER and EDITOR can create activity
            tripAccessService.assertCanEdit(tripId, username);

            // Get current user for createdBy
            User user = userRepository.findByUsernameAndActive(username)
                    .orElseThrow(() -> new BusinessException(USER_NOT_FOUND, COMMON.name()));

            // Get destination by trip ID and destination ID
            DestinationEntity destination = destinationRepository
                    .findByDestinationIdAndTrip_TripId(destinationId, tripId)
                    .orElseThrow(() -> new BusinessException(DESTINATION_NOT_FOUND, DESTINATION.name()));

            // Validate that activity time is within the destination date range
            activityValidator.validateActivityInsideDestination(
                    activityDTO.getStartDateTime(),
                    activityDTO.getEndDateTime(),
                    destination
            );

            // Check for overlapping activities in the same trip
            boolean hasOverlap = activityRepository
                    .existsByDestination_Trip_TripIdAndStartDateTimeLessThanAndEndDateTimeGreaterThan(
                            tripId,
                            activityDTO.getEndDateTime(),
                            activityDTO.getStartDateTime()
                    );

            if (hasOverlap) {
                log.error("Activity time overlaps with another activity in trip {}.", tripId);
                throw new BusinessException(
                        ACTIVITY_TIME_CONFLICT_WITH_EXISTING_ACTIVITY,
                        ACTIVITY.name()
                );
            }

            // Create activity
            ActivityEntity activity = new ActivityEntity(
                    activityName,
                    activityDTO.getLocation(),
                    activityDTO.getDescription(),
                    activityDTO.getStartDateTime(),
                    activityDTO.getEndDateTime(),
                    LocalDateTime.now(),
                    destination,
                    user
            );

            activityRepository.save(activity);
            return getCompleteResponse(
                    errorCodeRepository,
                    ACTIVITY_CREATED_SUCCESS,
                    ACTIVITY.name(),
                    activityMapper.toResponseDTO(activity)
            );

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while creating activity", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }

    @Override
    public CompleteResponse<Object> getActivitiesByDestination(Long tripId, Long destinationId) {
        try {
            log.info("Getting activities for tripId: {}, destinationId: {}", tripId, destinationId);

            if (tripId == null || destinationId == null) {
                log.error("Invalid input to get activity list!");
                throw new BusinessException(INVALID_INPUT, COMMON.name());
            }

            String username = authenticatedUserProvider.getUsername();

            // OWNER, EDITOR and VIEWER can view activities
            tripAccessService.assertCanView(tripId, username);

            // Check destination exists in this trip
            destinationRepository.findByDestinationIdAndTrip_TripId(destinationId, tripId)
                    .orElseThrow(() -> new BusinessException(DESTINATION_NOT_FOUND, DESTINATION.name()));

            // Get activities by destination and trip
            List<ActivityResponseDTO> activities = activityRepository
                    .findAllByDestination_DestinationIdAndDestination_Trip_TripId(
                            destinationId,
                            tripId
                    )
                    .stream()
                    .map(activityMapper::toResponseDTO)
                    .toList();

            return getCompleteResponse(
                    errorCodeRepository,
                    ACTIVITY_RETRIEVED_SUCCESS,
                    ACTIVITY.name(),
                    activities
            );

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while getting activities", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }

    @Override
    public CompleteResponse<Object> getActivityById(
            Long tripId,
            Long destinationId,
            Long activityId
    ) {
        try {
            log.info(
                    "Getting activity for tripId: {}, destinationId: {}, activityId: {}",
                    tripId,
                    destinationId,
                    activityId
            );

            if (tripId == null || destinationId == null || activityId == null) {
                log.error("Trip ID, Destination ID or Activity ID is missing to get an activity!");
                throw new BusinessException(INVALID_INPUT, COMMON.name());
            }

            String username = authenticatedUserProvider.getUsername();

            // OWNER, EDITOR and VIEWER can view activity
            tripAccessService.assertCanView(tripId, username);

            // Get activity by activity ID, destination ID and trip ID
            ActivityEntity activity = activityRepository
                    .findByActivityIdAndDestination_DestinationIdAndDestination_Trip_TripId(
                            activityId,
                            destinationId,
                            tripId
                    )
                    .orElseThrow(() -> new BusinessException(ACTIVITY_NOT_FOUND, ACTIVITY.name()));

            return getCompleteResponse(
                    errorCodeRepository,
                    ACTIVITY_RETRIEVED_SUCCESS,
                    ACTIVITY.name(),
                    activityMapper.toResponseDTO(activity)
            );

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while getting activity by ID", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }

    @Override
    public CompleteResponse<Object> updateActivity(
            Long tripId,
            Long destinationId,
            Long activityId,
            UpdateActivityDTO updateActivityDTO) {
        try {
            log.info("Updating activity for tripId: {}, destinationId: {}, activityId: {}", tripId, destinationId, activityId);

            // Validate input and get normalized activity name
            String activityName = activityValidator.validateUpdateInput(
                    tripId,
                    destinationId,
                    activityId,
                    updateActivityDTO
            );

            String location = normalizeKeyword(updateActivityDTO.getLocation());
            String username = authenticatedUserProvider.getUsername();

            // OWNER and EDITOR can update activity
            tripAccessService.assertCanEdit(tripId, username);

            // Get current user for modifiedBy
            User user = userRepository.findByUsernameAndActive(username)
                    .orElseThrow(() -> new BusinessException(USER_NOT_FOUND, COMMON.name()));

            // Get activity by activity ID, destination ID and trip ID
            ActivityEntity activity = activityRepository
                    .findByActivityIdAndDestination_DestinationIdAndDestination_Trip_TripId(
                            activityId,
                            destinationId,
                            tripId
                    )
                    .orElseThrow(() -> new BusinessException(ACTIVITY_NOT_FOUND, ACTIVITY.name()));

            DestinationEntity destination = activity.getDestination();

            // Validate that updated activity time is within the destination date range
            activityValidator.validateActivityInsideDestination(
                    updateActivityDTO.getStartDateTime(),
                    updateActivityDTO.getEndDateTime(),
                    destination
            );

            // Check for overlapping activities in the same trip, excluding current activity
            boolean hasOverlap = activityRepository.existsByDestination_Trip_TripIdAndActivityIdNotAndStartDateTimeLessThanAndEndDateTimeGreaterThan(
                            tripId,
                            activityId,
                            updateActivityDTO.getEndDateTime(),
                            updateActivityDTO.getStartDateTime()
                    );

            if (hasOverlap) {
                log.error("Updated activity time overlaps with another activity in trip {}.", tripId);
                throw new BusinessException(
                        ACTIVITY_TIME_CONFLICT_WITH_EXISTING_ACTIVITY,
                        ACTIVITY.name()
                );
            }

            // Update activity
            activity.setActivityName(activityName);
            activity.setLocation(location);
            activity.setDescription(updateActivityDTO.getDescription());
            activity.setStartDateTime(updateActivityDTO.getStartDateTime());
            activity.setEndDateTime(updateActivityDTO.getEndDateTime());
            activity.setModifiedBy(user);
            activity.setModifiedDate(LocalDateTime.now());

            activityRepository.save(activity);

            return getCompleteResponse(
                    errorCodeRepository,
                    ACTIVITY_UPDATED_SUCCESS,
                    ACTIVITY.name(),
                    activityMapper.toResponseDTO(activity)
            );

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while updating activity", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }

    @Override
    public CompleteResponse<Object> deleteActivity(Long tripId, Long destinationId, Long activityId) {
        try {
            log.info(
                    "Deleting activity for tripId: {}, destinationId: {}, activityId: {}",
                    tripId,
                    destinationId,
                    activityId
            );

            if (tripId == null || destinationId == null || activityId == null) {
                log.error("Trip ID, Destination ID or Activity ID is missing to delete an activity!");
                throw new BusinessException(INVALID_INPUT, COMMON.name());
            }

            String username = authenticatedUserProvider.getUsername();

            // OWNER and EDITOR can delete activity
            tripAccessService.assertCanEdit(tripId, username);

            // Get activity by activity ID, destination ID and trip ID
            ActivityEntity activity = activityRepository
                    .findByActivityIdAndDestination_DestinationIdAndDestination_Trip_TripId(
                            activityId,
                            destinationId,
                            tripId
                    )
                    .orElseThrow(() -> new BusinessException(ACTIVITY_NOT_FOUND, ACTIVITY.name()));

            // Delete activity
            activityRepository.delete(activity);

            return getCompleteResponse(
                    errorCodeRepository,
                    ACTIVITY_DELETED_SUCCESS,
                    ACTIVITY.name(),
                    null
            );

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while deleting activity", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }
}