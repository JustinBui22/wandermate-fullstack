package com.example.travellingapp.service.impl;

import com.example.travellingapp.dto.request.create.GenerateTripShareCodeRequest;
import com.example.travellingapp.entity.TripEntity;
import com.example.travellingapp.entity.User;
import com.example.travellingapp.entity.collaboration.TripCollaborationRequestEntity;
import com.example.travellingapp.entity.collaboration.TripShareCodeAttemptEntity;
import com.example.travellingapp.entity.collaboration.TripShareCodeEntity;
import com.example.travellingapp.enums.TripEnum;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.mapper.TripCollaborationRequestMapper;
import com.example.travellingapp.mapper.TripShareCodeMapper;
import com.example.travellingapp.repository.ErrorCodeRepository;
import com.example.travellingapp.repository.UserRepository;
import com.example.travellingapp.repository.collaboration.TripCollaborationRequestRepository;
import com.example.travellingapp.repository.collaboration.TripShareCodeAttemptRepository;
import com.example.travellingapp.repository.collaboration.TripShareCodeRepository;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.security.data_security.AuthenticatedUserProvider;
import com.example.travellingapp.service.TripAccessService;
import com.example.travellingapp.service.TripShareCodeService;
import com.example.travellingapp.validator.TripShareCodeValidator;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.example.travellingapp.enums.CommonEnum.COMMON;
import static com.example.travellingapp.enums.CommonEnum.TRIP_MEMBER;
import static com.example.travellingapp.enums.ErrorCodeEnum.*;
import static com.example.travellingapp.response_template.CompleteResponse.getCompleteResponse;

@Service
@Log4j2
public class TripShareCodeServiceImpl implements TripShareCodeService {

    private static final int MAX_INVALID_ATTEMPT = 5;
    private static final int SHARE_CODE_LENGTH = 8;
    private static final long CODE_EXPIRY_HOURS = 24;
    private static final long GENERATE_COOLDOWN_SECONDS = 60;
    private static final long ATTEMPT_RESTRICTION_MINUTES = 10;
    private static final String CODE_PREFIX = "WM-";

    private final TripShareCodeRepository tripShareCodeRepository;
    private final TripShareCodeAttemptRepository tripShareCodeAttemptRepository;
    private final TripCollaborationRequestRepository tripCollaborationRequestRepository;
    private final UserRepository userRepository;
    private final ErrorCodeRepository errorCodeRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final TripAccessService tripAccessService;
    private final TripCollaborationRequestMapper tripCollaborationRequestMapper;
    private final TripShareCodeMapper tripShareCodeMapper;
    private final TripShareCodeValidator tripShareCodeValidator;

    public TripShareCodeServiceImpl(
            TripShareCodeRepository tripShareCodeRepository,
            TripShareCodeAttemptRepository tripShareCodeAttemptRepository,
            TripCollaborationRequestRepository tripCollaborationRequestRepository,
            UserRepository userRepository,
            ErrorCodeRepository errorCodeRepository,
            AuthenticatedUserProvider authenticatedUserProvider,
            TripAccessService tripAccessService,
            TripCollaborationRequestMapper tripCollaborationRequestMapper,
            TripShareCodeMapper tripShareCodeMapper,
            TripShareCodeValidator tripShareCodeValidator
    ) {
        this.tripShareCodeRepository = tripShareCodeRepository;
        this.tripShareCodeAttemptRepository = tripShareCodeAttemptRepository;
        this.tripCollaborationRequestRepository = tripCollaborationRequestRepository;
        this.userRepository = userRepository;
        this.errorCodeRepository = errorCodeRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.tripAccessService = tripAccessService;
        this.tripCollaborationRequestMapper = tripCollaborationRequestMapper;
        this.tripShareCodeMapper = tripShareCodeMapper;
        this.tripShareCodeValidator = tripShareCodeValidator;
    }

    @Transactional
    @Override
    public CompleteResponse<Object> regenerateShareCode(
            Long tripId,
            GenerateTripShareCodeRequest request
    ) {
        try {
            // Validate trip ID
            tripShareCodeValidator.validateTripId(tripId);

            // Get current logged-in owner
            String username = authenticatedUserProvider.getUsername();

            // Only trip owner can generate share code
            TripEntity trip = tripAccessService.getTripIfOwner(tripId, username);

            // Get owner user record
            User owner = userRepository.findByUsernameAndActive(username)
                    .orElseThrow(() -> new BusinessException(USER_NOT_FOUND, COMMON.name()));

            // Resolve default role for people joining by code
            TripEnum defaultRole = tripShareCodeValidator.resolveDefaultRole(request);

            // Revoke or expire old active code before creating a new one
            tripShareCodeRepository
                    .findFirstByTrip_TripIdAndCodeStatusOrderByCreatedDateDesc(
                            tripId,
                            TripEnum.ACTIVE
                    )
                    .ifPresent(this::handleExistingActiveCodeBeforeRegeneration);

            // Generate new unique code
            String code = generateUniqueShareCode();
            LocalDateTime now = LocalDateTime.now();

            // Create new active share code
            TripShareCodeEntity shareCode = tripShareCodeMapper.toNewShareCodeEntity(
                    trip,
                    code,
                    owner,
                    defaultRole,
                    now,
                    CODE_EXPIRY_HOURS
            );

            // Save generated code
            TripShareCodeEntity savedShareCode = tripShareCodeRepository.save(shareCode);

            return getCompleteResponse(
                    errorCodeRepository,
                    TRIP_SHARE_CODE_CREATED_SUCCESS,
                    TRIP_MEMBER.name(),
                    tripShareCodeMapper.toResponseDTO(savedShareCode)
            );

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while regenerating trip share code", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }

    @Override
    public CompleteResponse<Object> previewShareCode(String code) {
        try {
            // Get current logged-in user
            User user = getCurrentActiveUser();

            // Check if user is temporarily restricted from trying codes
            validateAttemptIsNotRestricted(user);

            // Get valid active code
            TripShareCodeEntity shareCode = getValidShareCodeOrRegisterInvalidAttempt(
                    code,
                    user
            );

            return getCompleteResponse(
                    errorCodeRepository,
                    TRIP_SHARE_CODE_RETRIEVED_SUCCESS,
                    TRIP_MEMBER.name(),
                    tripShareCodeMapper.toPreviewResponseDTO(shareCode)
            );

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while previewing trip share code", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }

    @Transactional
    @Override
    public CompleteResponse<Object> requestToJoinByShareCode(String code) {
        try {
            // Get current logged-in user
            User requester = getCurrentActiveUser();

            // Check if user is temporarily restricted from trying codes
            validateAttemptIsNotRestricted(requester);

            // Get valid active code
            TripShareCodeEntity shareCode = getValidShareCodeOrRegisterInvalidAttempt(
                    code,
                    requester
            );

            // Validate requester can join this trip through share code
            tripShareCodeValidator.validateRequesterCanRequestToJoinByShareCode(
                    shareCode,
                    requester
            );

            // Create normal pending join request
            TripCollaborationRequestEntity joinRequest =
                    tripShareCodeMapper.toJoinRequestEntity(
                            shareCode,
                            requester,
                            LocalDateTime.now()
                    );

            // Save join request
            TripCollaborationRequestEntity savedJoinRequest =
                    tripCollaborationRequestRepository.save(joinRequest);

            // Mark share code as used after successful join request creation
            markShareCodeAsUsed(
                    shareCode,
                    requester
            );

            // Reset failed-code attempts after successful use
            resetInvalidAttempt(requester);

            return getCompleteResponse(
                    errorCodeRepository,
                    TRIP_SHARE_CODE_JOIN_REQUEST_SENT_SUCCESS,
                    TRIP_MEMBER.name(),
                    tripCollaborationRequestMapper.toResponseDTO(savedJoinRequest)
            );

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while requesting to join trip by share code", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }

    @Override
    public CompleteResponse<Object> getActiveShareCode(Long tripId) {
        try {
            // Validate trip ID
            tripShareCodeValidator.validateTripId(tripId);

            // Get current logged-in owner
            String username = authenticatedUserProvider.getUsername();

            // Only trip owner can view active share code
            tripAccessService.getTripIfOwner(tripId, username);

            // Find latest active share code for this trip
            TripShareCodeEntity activeShareCode = tripShareCodeRepository
                    .findFirstByTrip_TripIdAndCodeStatusOrderByCreatedDateDesc(
                            tripId,
                            TripEnum.ACTIVE
                    )
                    .orElse(null);

            // Return null if there is no active share code
            if (activeShareCode == null) {
                return getCompleteResponse(
                        errorCodeRepository,
                        TRIP_SHARE_CODE_RETRIEVED_SUCCESS,
                        TRIP_MEMBER.name(),
                        null
                );
            }

            LocalDateTime now = LocalDateTime.now();

            // If active code expired, mark it as expired and return null
            if (activeShareCode.getExpiresAt().isBefore(now)) {
                activeShareCode.setCodeStatus(TripEnum.EXPIRED);
                activeShareCode.setModifiedDate(now);
                tripShareCodeRepository.save(activeShareCode);

                return getCompleteResponse(
                        errorCodeRepository,
                        TRIP_SHARE_CODE_RETRIEVED_SUCCESS,
                        TRIP_MEMBER.name(),
                        null
                );
            }

            // Return active share code
            return getCompleteResponse(
                    errorCodeRepository,
                    TRIP_SHARE_CODE_RETRIEVED_SUCCESS,
                    TRIP_MEMBER.name(),
                    tripShareCodeMapper.toResponseDTO(activeShareCode)
            );

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while getting active trip share code", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }

    private void handleExistingActiveCodeBeforeRegeneration(TripShareCodeEntity activeCode) {
        LocalDateTime now = LocalDateTime.now();

        // Validate whether current active code can be replaced
        tripShareCodeValidator.validateActiveCodeCanBeRegenerated(
                activeCode,
                now,
                GENERATE_COOLDOWN_SECONDS
        );

        // If active code already expired, mark it as expired
        if (activeCode.getExpiresAt().isBefore(now)) {
            activeCode.setCodeStatus(TripEnum.EXPIRED);
            activeCode.setModifiedDate(now);
            tripShareCodeRepository.save(activeCode);
            return;
        }

        // Revoke old active code when owner regenerates after cooldown
        activeCode.setCodeStatus(TripEnum.REVOKED);
        activeCode.setModifiedDate(now);
        tripShareCodeRepository.save(activeCode);
    }

    private TripShareCodeEntity getValidShareCodeOrRegisterInvalidAttempt(
            String code,
            User user
    ) {
        // Normalize code input
        String normalizedCode = tripShareCodeValidator.normalizeCode(code);

        // Check if share code exists
        TripShareCodeEntity shareCode = tripShareCodeRepository.findByCode(normalizedCode)
                .orElseGet(() -> {
                    registerInvalidAttempt(user);
                    throw new BusinessException(TRIP_SHARE_CODE_NOT_FOUND, TRIP_MEMBER.name());
                });

        try {
            // Check if share code is active and not expired
            tripShareCodeValidator.validateShareCodeCanBeUsed(
                    shareCode,
                    LocalDateTime.now()
            );

            // Reset failed-code attempts after a valid code is entered
            resetInvalidAttempt(user);

            return shareCode;
        } catch (BusinessException e) {
            // Expired active code should be stored as EXPIRED
            if (e.getErrorCodeEnum() == TRIP_SHARE_CODE_EXPIRED
                    && shareCode.getCodeStatus() == TripEnum.ACTIVE) {
                shareCode.setCodeStatus(TripEnum.EXPIRED);
                shareCode.setModifiedDate(LocalDateTime.now());
                tripShareCodeRepository.save(shareCode);
            }

            registerInvalidAttempt(user);
            throw e;
        }
    }

    private void markShareCodeAsUsed(
            TripShareCodeEntity shareCode,
            User requester
    ) {
        // Mark code as used by this requester
        shareCode.setCodeStatus(TripEnum.USED);
        shareCode.setUsedByUser(requester);
        shareCode.setUsedDate(LocalDateTime.now());
        shareCode.setModifiedDate(LocalDateTime.now());

        tripShareCodeRepository.save(shareCode);
    }

    private void validateAttemptIsNotRestricted(User user) {
        LocalDateTime now = LocalDateTime.now();

        tripShareCodeAttemptRepository
                .findByUser_UserId(user.getUserId())
                .ifPresent(attempt -> {
                    // Validate active restriction
                    tripShareCodeValidator.validateAttemptIsNotRestricted(
                            attempt,
                            now
                    );

                    // Clear expired restriction
                    if (
                            attempt.getRestrictedUntil() != null
                                    && !attempt.getRestrictedUntil().isAfter(now)
                    ) {
                        attempt.setRetryCount(0);
                        attempt.setRestrictedUntil(null);
                        attempt.setModifiedDate(now);
                        tripShareCodeAttemptRepository.save(attempt);
                    }
                });
    }

    private void registerInvalidAttempt(User user) {
        LocalDateTime now = LocalDateTime.now();

        // Find existing attempt record or create a new one
        TripShareCodeAttemptEntity attempt = tripShareCodeAttemptRepository
                .findByUser_UserId(user.getUserId())
                .orElseGet(() -> new TripShareCodeAttemptEntity(
                        user,
                        0,
                        now
                ));

        // Reset old expired restriction before counting again
        if (
                attempt.getRestrictedUntil() != null
                        && !attempt.getRestrictedUntil().isAfter(now)
        ) {
            attempt.setRetryCount(0);
            attempt.setRestrictedUntil(null);
        }

        // Increase invalid attempt count
        int newRetryCount = attempt.getRetryCount() + 1;
        attempt.setRetryCount(newRetryCount);
        attempt.setLastAttemptDate(now);
        attempt.setModifiedDate(now);

        // Restrict user after too many invalid attempts
        if (newRetryCount >= MAX_INVALID_ATTEMPT) {
            attempt.setRetryCount(0);
            attempt.setRestrictedUntil(now.plusMinutes(ATTEMPT_RESTRICTION_MINUTES));
        }

        tripShareCodeAttemptRepository.save(attempt);
    }

    private void resetInvalidAttempt(User user) {
        LocalDateTime now = LocalDateTime.now();

        tripShareCodeAttemptRepository
                .findByUser_UserId(user.getUserId())
                .ifPresent(attempt -> {
                    // Reset invalid attempt count after successful valid code
                    attempt.setRetryCount(0);
                    attempt.setRestrictedUntil(null);
                    attempt.setLastAttemptDate(now);
                    attempt.setModifiedDate(now);
                    tripShareCodeAttemptRepository.save(attempt);
                });
    }

    private User getCurrentActiveUser() {
        // Get current logged-in username
        String username = authenticatedUserProvider.getUsername();

        // Find active user
        return userRepository.findByUsernameAndActive(username)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND, COMMON.name()));
    }

    private String generateUniqueShareCode() {
        String code;

        // Keep generating until the code is unique
        do {
            code = CODE_PREFIX + UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, SHARE_CODE_LENGTH)
                    .toUpperCase();
        } while (tripShareCodeRepository.existsByCode(code));

        return code;
    }
}