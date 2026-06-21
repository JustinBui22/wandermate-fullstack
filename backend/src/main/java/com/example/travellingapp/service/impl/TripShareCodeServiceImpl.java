package com.example.travellingapp.service.impl;

import com.example.travellingapp.dto.request.create.GenerateTripShareCodeRequest;
import com.example.travellingapp.dto.response.TripShareCodePreviewResponseDTO;
import com.example.travellingapp.dto.response.TripShareCodeResponseDTO;
import com.example.travellingapp.entity.TripEntity;
import com.example.travellingapp.entity.User;
import com.example.travellingapp.entity.collaboration.TripCollaborationRequestEntity;
import com.example.travellingapp.entity.collaboration.TripShareCodeAttemptEntity;
import com.example.travellingapp.entity.collaboration.TripShareCodeEntity;
import com.example.travellingapp.enums.TripEnum;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.mapper.TripCollaborationRequestMapper;
import com.example.travellingapp.repository.ErrorCodeRepository;
import com.example.travellingapp.repository.UserRepository;
import com.example.travellingapp.repository.collaboration.TripCollaborationRequestRepository;
import com.example.travellingapp.repository.collaboration.TripShareCodeAttemptRepository;
import com.example.travellingapp.repository.collaboration.TripShareCodeRepository;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.security.data_security.AuthenticatedUserProvider;
import com.example.travellingapp.service.TripAccessService;
import com.example.travellingapp.service.TripShareCodeService;
import com.example.travellingapp.validator.TripCollaborationRequestValidator;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static com.example.travellingapp.enums.CommonEnum.COMMON;
import static com.example.travellingapp.enums.CommonEnum.TRIP;
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
    private static final String INVITE_LINK_PREFIX = "wandermate://join-trip?code=";

    private final TripShareCodeRepository tripShareCodeRepository;
    private final TripShareCodeAttemptRepository tripShareCodeAttemptRepository;
    private final TripCollaborationRequestRepository tripCollaborationRequestRepository;
    private final UserRepository userRepository;
    private final ErrorCodeRepository errorCodeRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final TripAccessService tripAccessService;
    private final TripCollaborationRequestValidator tripCollaborationRequestValidator;
    private final TripCollaborationRequestMapper tripCollaborationRequestMapper;

    public TripShareCodeServiceImpl(
            TripShareCodeRepository tripShareCodeRepository,
            TripShareCodeAttemptRepository tripShareCodeAttemptRepository,
            TripCollaborationRequestRepository tripCollaborationRequestRepository,
            UserRepository userRepository,
            ErrorCodeRepository errorCodeRepository,
            AuthenticatedUserProvider authenticatedUserProvider,
            TripAccessService tripAccessService,
            TripCollaborationRequestValidator tripCollaborationRequestValidator,
            TripCollaborationRequestMapper tripCollaborationRequestMapper
    ) {
        this.tripShareCodeRepository = tripShareCodeRepository;
        this.tripShareCodeAttemptRepository = tripShareCodeAttemptRepository;
        this.tripCollaborationRequestRepository = tripCollaborationRequestRepository;
        this.userRepository = userRepository;
        this.errorCodeRepository = errorCodeRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.tripAccessService = tripAccessService;
        this.tripCollaborationRequestValidator = tripCollaborationRequestValidator;
        this.tripCollaborationRequestMapper = tripCollaborationRequestMapper;
    }

    @Transactional
    @Override
    public CompleteResponse<Object> regenerateShareCode(
            Long tripId,
            GenerateTripShareCodeRequest request
    ) {
        try {
            // Validate trip ID
            if (tripId == null) {
                throw new BusinessException(INVALID_INPUT, COMMON.name());
            }

            // Get current logged-in owner
            String username = authenticatedUserProvider.getUsername();

            // Only trip owner can generate share code
            TripEntity trip = tripAccessService.getTripIfOwner(tripId, username);

            // Get owner user record
            User owner = userRepository.findByUsernameAndActive(username)
                    .orElseThrow(() -> new BusinessException(USER_NOT_FOUND, COMMON.name()));

            // Resolve default role for people joining by code
            TripEnum defaultRole = resolveDefaultRole(request);

            // Find current active code for this trip
            tripShareCodeRepository
                    .findFirstByTrip_TripIdAndCodeStatusOrderByCreatedDateDesc(
                            tripId,
                            TripEnum.ACTIVE
                    )
                    .ifPresent(activeCode -> handleExistingActiveCodeBeforeRegeneration(activeCode));

            // Generate new unique code
            String code = generateUniqueShareCode();

            // Create new active share code
            TripShareCodeEntity shareCode = new TripShareCodeEntity(
                    trip,
                    code,
                    owner,
                    defaultRole,
                    TripEnum.ACTIVE,
                    LocalDateTime.now().plusHours(CODE_EXPIRY_HOURS),
                    LocalDateTime.now()
            );

            // Save generated code
            TripShareCodeEntity savedShareCode = tripShareCodeRepository.save(shareCode);

            return getCompleteResponse(
                    errorCodeRepository,
                    TRIP_SHARE_CODE_CREATED_SUCCESS,
                    TRIP_MEMBER.name(),
                    toShareCodeResponseDTO(savedShareCode)
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
                    toPreviewResponseDTO(shareCode)
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

            TripEntity trip = shareCode.getTrip();
            User owner = trip.getUser();

            // Owner cannot request to join their own trip
            tripCollaborationRequestValidator.validateOwnerCannotRequestToJoinOwnTrip(
                    owner,
                    requester
            );

            // Requester must not already be a trip member
            tripCollaborationRequestValidator.validateUserIsNotAlreadyMember(
                    trip.getTripId(),
                    requester
            );

            // Avoid duplicate pending invitation or join request between requester and owner
            tripCollaborationRequestValidator.validateNoPendingRequestBetweenUsers(
                    trip.getTripId(),
                    requester,
                    owner
            );

            // Create normal pending join request
            TripCollaborationRequestEntity joinRequest = new TripCollaborationRequestEntity(
                    trip,
                    requester,
                    owner,
                    toCollaborationRole(shareCode.getDefaultRole()),
                    TripEnum.JOIN_REQUEST,
                    TripEnum.PENDING,
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

    private void handleExistingActiveCodeBeforeRegeneration(TripShareCodeEntity activeCode) {
        LocalDateTime now = LocalDateTime.now();

        // If active code already expired, mark it as expired and allow new code immediately
        if (activeCode.getExpiresAt().isBefore(now)) {
            activeCode.setCodeStatus(TripEnum.EXPIRED);
            activeCode.setModifiedDate(now);
            tripShareCodeRepository.save(activeCode);
            return;
        }

        // Cooldown blocks only active unused code
        if (activeCode.getCreatedDate().plusSeconds(GENERATE_COOLDOWN_SECONDS).isAfter(now)) {
            throw new BusinessException(TRIP_SHARE_CODE_GENERATE_TOO_SOON, TRIP_MEMBER.name());
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
        String normalizedCode = normalizeCode(code);

        // Check if share code exists
        TripShareCodeEntity shareCode = tripShareCodeRepository.findByCode(normalizedCode)
                .orElseGet(() -> {
                    registerInvalidAttempt(user);
                    throw new BusinessException(TRIP_SHARE_CODE_NOT_FOUND, TRIP_MEMBER.name());
                });

        try {
            // Check if share code is active and not expired
            validateShareCodeCanBeUsed(shareCode);

            // Reset failed-code attempts after a valid code is entered
            resetInvalidAttempt(user);

            return shareCode;
        } catch (BusinessException e) {
            registerInvalidAttempt(user);
            throw e;
        }
    }

    private void validateShareCodeCanBeUsed(TripShareCodeEntity shareCode) {
        LocalDateTime now = LocalDateTime.now();

        // Used code cannot be reused
        if (shareCode.getCodeStatus() == TripEnum.USED) {
            throw new BusinessException(TRIP_SHARE_CODE_USED, TRIP_MEMBER.name());
        }

        // Revoked code cannot be used
        if (shareCode.getCodeStatus() == TripEnum.REVOKED) {
            throw new BusinessException(TRIP_SHARE_CODE_REVOKED, TRIP_MEMBER.name());
        }

        // Expired code cannot be used
        if (shareCode.getCodeStatus() == TripEnum.EXPIRED) {
            throw new BusinessException(TRIP_SHARE_CODE_EXPIRED, TRIP_MEMBER.name());
        }

        // Any non-active status is invalid
        if (shareCode.getCodeStatus() != TripEnum.ACTIVE) {
            throw new BusinessException(TRIP_SHARE_CODE_INACTIVE, TRIP_MEMBER.name());
        }

        // If active code passed expiry time, mark it as expired
        if (shareCode.getExpiresAt().isBefore(now)) {
            shareCode.setCodeStatus(TripEnum.EXPIRED);
            shareCode.setModifiedDate(now);
            tripShareCodeRepository.save(shareCode);
            throw new BusinessException(TRIP_SHARE_CODE_EXPIRED, TRIP_MEMBER.name());
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
                    // Block user if restriction time has not passed
                    if (
                            attempt.getRestrictedUntil() != null
                                    && attempt.getRestrictedUntil().isAfter(now)
                    ) {
                        throw new BusinessException(
                                TRIP_SHARE_CODE_ATTEMPT_RESTRICTED,
                                TRIP_MEMBER.name()
                        );
                    }

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

    private TripEnum resolveDefaultRole(GenerateTripShareCodeRequest request) {
        // Default share-code role is VIEWER
        if (request == null || request.getDefaultRole() == null) {
            return TripEnum.VIEWER;
        }

        // Share-code role can only be EDITOR or VIEWER
        if (
                request.getDefaultRole() == TripEnum.EDITOR
                        || request.getDefaultRole() == TripEnum.VIEWER
        ) {
            return request.getDefaultRole();
        }

        throw new BusinessException(TRIP_OWNER_ROLE_CANNOT_BE_CHANGED, TRIP_MEMBER.name());
    }

    private TripEnum toCollaborationRole(TripEnum role) {
        // Convert share-code default role to collaboration request role
        if (role == TripEnum.EDITOR) {
            return TripEnum.EDITOR;
        }

        return TripEnum.VIEWER;
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

    private String normalizeCode(String code) {
        // Validate share code input
        if (code == null || code.isBlank()) {
            throw new BusinessException(INVALID_INPUT, COMMON.name());
        }

        return code.trim().toUpperCase();
    }

    private String buildInviteLink(String code) {
        return INVITE_LINK_PREFIX + code;
    }

    private TripShareCodeResponseDTO toShareCodeResponseDTO(TripShareCodeEntity shareCode) {
        // Build response for owner after generating share code
        return new TripShareCodeResponseDTO(
                shareCode.getTrip().getTripId(),
                shareCode.getTrip().getTripName(),
                shareCode.getCode(),
                buildInviteLink(shareCode.getCode()),
                shareCode.getDefaultRole(),
                shareCode.getCodeStatus(),
                shareCode.getExpiresAt(),
                shareCode.getCreatedDate()
        );
    }

    private TripShareCodePreviewResponseDTO toPreviewResponseDTO(TripShareCodeEntity shareCode) {
        TripEntity trip = shareCode.getTrip();

        // Build preview response before user requests to join
        return new TripShareCodePreviewResponseDTO(
                trip.getTripId(),
                trip.getTripName(),
                trip.getDestination(),
                trip.getStartDate(),
                trip.getEndDate(),
                trip.getUser().getUsername(),
                shareCode.getDefaultRole(),
                shareCode.getExpiresAt()
        );
    }
}