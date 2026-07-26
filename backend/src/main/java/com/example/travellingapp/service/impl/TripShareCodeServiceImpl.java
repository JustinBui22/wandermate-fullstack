package com.example.travellingapp.service.impl;

import com.example.travellingapp.dto.request.create.GenerateTripShareCodeRequest;
import com.example.travellingapp.entity.ConfigurationEntity;
import com.example.travellingapp.entity.TripEntity;
import com.example.travellingapp.entity.User;
import com.example.travellingapp.entity.collaboration.TripCollaborationRequestEntity;
import com.example.travellingapp.entity.collaboration.TripShareCodeEntity;
import com.example.travellingapp.enums.TripEnum;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.exception_handler.exception.TripShareCodeExpiredException;
import com.example.travellingapp.mapper.TripCollaborationRequestMapper;
import com.example.travellingapp.mapper.TripShareCodeMapper;
import com.example.travellingapp.repository.ConfigurationRepository;
import com.example.travellingapp.repository.ErrorCodeRepository;
import com.example.travellingapp.repository.TripRepository;
import com.example.travellingapp.repository.UserRepository;
import com.example.travellingapp.repository.collaboration.TripCollaborationRequestRepository;
import com.example.travellingapp.repository.collaboration.TripShareCodeAttemptRepository;
import com.example.travellingapp.repository.collaboration.TripShareCodeRepository;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.security.data_security.AuthenticatedUserProvider;
import com.example.travellingapp.service.TripAccessService;
import com.example.travellingapp.service.TripShareCodeSecurityEventService;
import com.example.travellingapp.service.TripShareCodeService;
import com.example.travellingapp.validator.TripShareCodeValidator;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import static com.example.travellingapp.enums.CommonEnum.COMMON;
import static com.example.travellingapp.enums.CommonEnum.INVITE_LINK_PREFIX;
import static com.example.travellingapp.enums.CommonEnum.TRIP_MEMBER;
import static com.example.travellingapp.enums.ErrorCodeEnum.INTERNAL_SERVER_ERROR;
import static com.example.travellingapp.enums.ErrorCodeEnum.TRIP_NOT_FOUND;
import static com.example.travellingapp.enums.ErrorCodeEnum.TRIP_SHARE_CODE_CREATED_SUCCESS;
import static com.example.travellingapp.enums.ErrorCodeEnum.TRIP_SHARE_CODE_EXPIRED;
import static com.example.travellingapp.enums.ErrorCodeEnum.TRIP_SHARE_CODE_JOIN_REQUEST_SENT_SUCCESS;
import static com.example.travellingapp.enums.ErrorCodeEnum.TRIP_SHARE_CODE_NOT_FOUND;
import static com.example.travellingapp.enums.ErrorCodeEnum.TRIP_SHARE_CODE_RETRIEVED_SUCCESS;
import static com.example.travellingapp.enums.ErrorCodeEnum.USER_NOT_FOUND;
import static com.example.travellingapp.response_template.CompleteResponse.getCompleteResponse;

@Service
@Log4j2
public class TripShareCodeServiceImpl
        implements TripShareCodeService {

    private static final int SHARE_CODE_LENGTH = 12;
    private static final long CODE_EXPIRY_HOURS = 24;
    private static final long GENERATE_COOLDOWN_SECONDS = 60;
    private static final String CODE_PREFIX = "WM-";
    private static final String CODE_ALPHABET =
            "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int MAX_CODE_GENERATION_ATTEMPTS = 10;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final TripShareCodeRepository tripShareCodeRepository;
    private final TripRepository tripRepository;
    private final ConfigurationRepository configurationRepository;
    private final TripShareCodeAttemptRepository
            tripShareCodeAttemptRepository;
    private final TripCollaborationRequestRepository
            tripCollaborationRequestRepository;
    private final UserRepository userRepository;
    private final ErrorCodeRepository errorCodeRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final TripAccessService tripAccessService;
    private final TripCollaborationRequestMapper
            tripCollaborationRequestMapper;
    private final TripShareCodeMapper tripShareCodeMapper;
    private final TripShareCodeValidator tripShareCodeValidator;
    private final TripShareCodeSecurityEventService
            tripShareCodeSecurityEventService;

    public TripShareCodeServiceImpl(
            TripShareCodeRepository tripShareCodeRepository,
            TripRepository tripRepository,
            ConfigurationRepository configurationRepository,
            TripShareCodeAttemptRepository
                    tripShareCodeAttemptRepository,
            TripCollaborationRequestRepository
                    tripCollaborationRequestRepository,
            UserRepository userRepository,
            ErrorCodeRepository errorCodeRepository,
            AuthenticatedUserProvider authenticatedUserProvider,
            TripAccessService tripAccessService,
            TripCollaborationRequestMapper
                    tripCollaborationRequestMapper,
            TripShareCodeMapper tripShareCodeMapper,
            TripShareCodeValidator tripShareCodeValidator,
            TripShareCodeSecurityEventService
                    tripShareCodeSecurityEventService
    ) {
        this.tripShareCodeRepository = tripShareCodeRepository;
        this.tripRepository = tripRepository;
        this.configurationRepository = configurationRepository;
        this.tripShareCodeAttemptRepository =
                tripShareCodeAttemptRepository;
        this.tripCollaborationRequestRepository =
                tripCollaborationRequestRepository;
        this.userRepository = userRepository;
        this.errorCodeRepository = errorCodeRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.tripAccessService = tripAccessService;
        this.tripCollaborationRequestMapper =
                tripCollaborationRequestMapper;
        this.tripShareCodeMapper = tripShareCodeMapper;
        this.tripShareCodeValidator = tripShareCodeValidator;
        this.tripShareCodeSecurityEventService =
                tripShareCodeSecurityEventService;
    }

    @Transactional
    @Override
    public CompleteResponse<Object> regenerateShareCode(
            Long tripId,
            GenerateTripShareCodeRequest request
    ) {
        try {
            tripShareCodeValidator.validateTripId(tripId);

            String username =
                    authenticatedUserProvider.getUsername();

            tripAccessService.assertIsOwner(tripId, username);

            TripEntity trip = tripRepository
                    .findByTripIdForUpdate(tripId)
                    .orElseThrow(
                            () -> new BusinessException(
                                    TRIP_NOT_FOUND,
                                    TRIP_MEMBER.name()
                            )
                    );

            User owner = userRepository
                    .findByUsernameAndActive(username)
                    .orElseThrow(
                            () -> new BusinessException(
                                    USER_NOT_FOUND,
                                    COMMON.name()
                            )
                    );

            TripEnum defaultRole =
                    tripShareCodeValidator.resolveDefaultRole(request);

            tripShareCodeRepository
                    .findFirstByTrip_TripIdAndCodeStatusOrderByCreatedDateDesc(
                            tripId,
                            TripEnum.ACTIVE
                    )
                    .ifPresent(
                            this::handleExistingActiveCodeBeforeRegeneration
                    );

            String code = generateUniqueShareCode();
            LocalDateTime now = LocalDateTime.now();

            TripShareCodeEntity shareCode =
                    tripShareCodeMapper.toNewShareCodeEntity(
                            trip,
                            code,
                            owner,
                            defaultRole,
                            now,
                            CODE_EXPIRY_HOURS
                    );

            TripShareCodeEntity savedShareCode =
                    tripShareCodeRepository.save(shareCode);

            String inviteLinkPrefix = configurationRepository
                    .findByConfigCode(INVITE_LINK_PREFIX.name())
                    .map(ConfigurationEntity::getConfigValue)
                    .orElseGet(() -> {
                        log.error(
                                "Invite link prefix configuration not found to regenerate trip share code"
                        );
                        return "wandermate://join-trip?code=";
                    });

            return getCompleteResponse(
                    errorCodeRepository,
                    TRIP_SHARE_CODE_CREATED_SUCCESS,
                    TRIP_MEMBER.name(),
                    tripShareCodeMapper.toResponseDTO(
                            savedShareCode,
                            inviteLinkPrefix
                    )
            );
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            log.error(
                    "Error occurred while regenerating trip share code",
                    exception
            );
            throw new BusinessException(
                    INTERNAL_SERVER_ERROR,
                    COMMON.name()
            );
        }
    }

    @Override
    public CompleteResponse<Object> previewShareCode(String code) {
        try {
            User user = getCurrentActiveUser();

            validateAttemptIsNotRestricted(user);

            TripShareCodeEntity shareCode =
                    getValidShareCodeOrRegisterInvalidAttempt(
                            code,
                            user,
                            false
                    );

            return getCompleteResponse(
                    errorCodeRepository,
                    TRIP_SHARE_CODE_RETRIEVED_SUCCESS,
                    TRIP_MEMBER.name(),
                    tripShareCodeMapper.toPreviewResponseDTO(
                            shareCode
                    )
            );
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            log.error(
                    "Error occurred while previewing trip share code",
                    exception
            );
            throw new BusinessException(
                    INTERNAL_SERVER_ERROR,
                    COMMON.name()
            );
        }
    }

    @Transactional(noRollbackFor = TripShareCodeExpiredException.class)
    @Override
    public CompleteResponse<Object> requestToJoinByShareCode(
            String code
    ) {
        try {
            User requester = getCurrentActiveUser();

            validateAttemptIsNotRestricted(requester);

            TripShareCodeEntity shareCode =
                    getValidShareCodeOrRegisterInvalidAttempt(
                            code,
                            requester,
                            true
                    );

            tripShareCodeValidator
                    .validateRequesterCanRequestToJoinByShareCode(
                            shareCode,
                            requester
                    );

            TripCollaborationRequestEntity joinRequest =
                    tripShareCodeMapper.toJoinRequestEntity(
                            shareCode,
                            requester,
                            LocalDateTime.now()
                    );

            TripCollaborationRequestEntity savedJoinRequest =
                    tripCollaborationRequestRepository.save(
                            joinRequest
                    );

            markShareCodeAsUsed(shareCode, requester);
            resetInvalidAttempt(requester);

            return getCompleteResponse(
                    errorCodeRepository,
                    TRIP_SHARE_CODE_JOIN_REQUEST_SENT_SUCCESS,
                    TRIP_MEMBER.name(),
                    tripCollaborationRequestMapper.toResponseDTO(
                            savedJoinRequest
                    )
            );
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            log.error(
                    "Error occurred while requesting to join trip by share code",
                    exception
            );
            throw new BusinessException(
                    INTERNAL_SERVER_ERROR,
                    COMMON.name()
            );
        }
    }

    @Override
    public CompleteResponse<Object> getActiveShareCode(Long tripId) {
        try {
            tripShareCodeValidator.validateTripId(tripId);

            String username =
                    authenticatedUserProvider.getUsername();

            tripAccessService.getTripIfOwner(tripId, username);

            TripShareCodeEntity activeShareCode =
                    tripShareCodeRepository
                            .findFirstByTrip_TripIdAndCodeStatusOrderByCreatedDateDesc(
                                    tripId,
                                    TripEnum.ACTIVE
                            )
                            .orElse(null);

            if (activeShareCode == null) {
                return getCompleteResponse(
                        errorCodeRepository,
                        TRIP_SHARE_CODE_RETRIEVED_SUCCESS,
                        TRIP_MEMBER.name(),
                        null
                );
            }

            LocalDateTime now = LocalDateTime.now();

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

            String inviteLinkPrefix = configurationRepository
                    .findByConfigCode(INVITE_LINK_PREFIX.name())
                    .map(ConfigurationEntity::getConfigValue)
                    .orElseGet(() -> {
                        log.error(
                                "Invite link prefix configuration not found to get active trip share code"
                        );
                        return "wandermate://join-trip?code=";
                    });

            return getCompleteResponse(
                    errorCodeRepository,
                    TRIP_SHARE_CODE_CREATED_SUCCESS,
                    TRIP_MEMBER.name(),
                    tripShareCodeMapper.toResponseDTO(
                            activeShareCode,
                            inviteLinkPrefix
                    )
            );
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            log.error(
                    "Error occurred while getting active trip share code",
                    exception
            );
            throw new BusinessException(
                    INTERNAL_SERVER_ERROR,
                    COMMON.name()
            );
        }
    }

    private void handleExistingActiveCodeBeforeRegeneration(
            TripShareCodeEntity activeCode
    ) {
        LocalDateTime now = LocalDateTime.now();

        tripShareCodeValidator.validateActiveCodeCanBeRegenerated(
                activeCode,
                now,
                GENERATE_COOLDOWN_SECONDS
        );

        if (activeCode.getExpiresAt().isBefore(now)) {
            activeCode.setCodeStatus(TripEnum.EXPIRED);
            activeCode.setModifiedDate(now);
            tripShareCodeRepository.saveAndFlush(activeCode);
            return;
        }

        activeCode.setCodeStatus(TripEnum.REVOKED);
        activeCode.setModifiedDate(now);
        tripShareCodeRepository.saveAndFlush(activeCode);
    }

    private TripShareCodeEntity
    getValidShareCodeOrRegisterInvalidAttempt(
            String code,
            User user,
            boolean lockForRedemption
    ) {
        String normalizedCode =
                tripShareCodeValidator.normalizeCode(code);

        TripShareCodeEntity shareCode =
                (lockForRedemption
                        ? tripShareCodeRepository
                        .findByCodeForUpdate(normalizedCode)
                        : tripShareCodeRepository
                        .findByCode(normalizedCode))
                        .orElseGet(() -> {
                            tripShareCodeSecurityEventService
                                    .recordInvalidAttempt(
                                            user.getUserId()
                                    );

                            log.error(
                                    "Trip share code not found for code: {}",
                                    normalizedCode
                            );

                            throw new BusinessException(
                                    TRIP_SHARE_CODE_NOT_FOUND,
                                    TRIP_MEMBER.name()
                            );
                        });

        try {
            tripShareCodeValidator.validateShareCodeCanBeUsed(
                    shareCode,
                    LocalDateTime.now()
            );

            resetInvalidAttempt(user);
            return shareCode;
        } catch (BusinessException exception) {
            if (exception.getErrorCodeEnum()
                    == TRIP_SHARE_CODE_EXPIRED
                    && shareCode.getCodeStatus()
                    == TripEnum.ACTIVE) {
                if (lockForRedemption) {
                    shareCode.setCodeStatus(TripEnum.EXPIRED);
                    shareCode.setModifiedDate(LocalDateTime.now());
                    tripShareCodeRepository.save(shareCode);
                    tripShareCodeSecurityEventService
                            .recordInvalidAttempt(user.getUserId());
                    throw new TripShareCodeExpiredException();
                }

                tripShareCodeSecurityEventService
                        .recordExpiredCodeAndInvalidAttempt(
                                shareCode.getShareCodeId(),
                                user.getUserId()
                        );
            } else {
                tripShareCodeSecurityEventService
                        .recordInvalidAttempt(user.getUserId());
            }

            throw exception;
        }
    }

    private void markShareCodeAsUsed(
            TripShareCodeEntity shareCode,
            User requester
    ) {
        LocalDateTime now = LocalDateTime.now();

        shareCode.setCodeStatus(TripEnum.USED);
        shareCode.setUsedByUser(requester);
        shareCode.setUsedDate(now);
        shareCode.setModifiedDate(now);

        tripShareCodeRepository.save(shareCode);
    }

    private void validateAttemptIsNotRestricted(User user) {
        LocalDateTime now = LocalDateTime.now();

        tripShareCodeAttemptRepository
                .findByUser_UserId(user.getUserId())
                .ifPresent(attempt -> {
                    tripShareCodeValidator
                            .validateAttemptIsNotRestricted(
                                    attempt,
                                    now
                            );

                    if (attempt.getRestrictedUntil() != null
                            && !attempt
                            .getRestrictedUntil()
                            .isAfter(now)) {
                        attempt.setRetryCount(0);
                        attempt.setRestrictedUntil(null);
                        attempt.setModifiedDate(now);
                        tripShareCodeAttemptRepository.save(attempt);
                    }
                });
    }

    private void resetInvalidAttempt(User user) {
        LocalDateTime now = LocalDateTime.now();

        tripShareCodeAttemptRepository
                .findByUser_UserId(user.getUserId())
                .ifPresent(attempt -> {
                    attempt.setRetryCount(0);
                    attempt.setRestrictedUntil(null);
                    attempt.setLastAttemptDate(now);
                    attempt.setModifiedDate(now);
                    tripShareCodeAttemptRepository.save(attempt);
                });
    }

    private User getCurrentActiveUser() {
        String username =
                authenticatedUserProvider.getUsername();

        return userRepository
                .findByUsernameAndActive(username)
                .orElseThrow(
                        () -> new BusinessException(
                                USER_NOT_FOUND,
                                COMMON.name()
                        )
                );
    }

    private String generateUniqueShareCode() {
        for (int attempt = 0;
             attempt < MAX_CODE_GENERATION_ATTEMPTS;
             attempt++) {
            StringBuilder codeBuilder = new StringBuilder(
                    CODE_PREFIX
            );

            for (int character = 0;
                 character < SHARE_CODE_LENGTH;
                 character++) {
                codeBuilder.append(
                        CODE_ALPHABET.charAt(
                                SECURE_RANDOM.nextInt(
                                        CODE_ALPHABET.length()
                                )
                        )
                );
            }

            String code = codeBuilder.toString();
            if (!tripShareCodeRepository.existsByCode(code)) {
                return code;
            }
        }

        log.error(
                "Unable to generate a unique trip share code after {} attempts",
                MAX_CODE_GENERATION_ATTEMPTS
        );
        throw new BusinessException(
                INTERNAL_SERVER_ERROR,
                COMMON.name()
        );
    }
}