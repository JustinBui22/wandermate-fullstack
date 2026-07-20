package com.example.travellingapp.service.impl;

import com.example.travellingapp.entity.User;
import com.example.travellingapp.entity.collaboration.TripShareCodeAttemptEntity;
import com.example.travellingapp.enums.TripEnum;
import com.example.travellingapp.repository.collaboration.TripShareCodeAttemptRepository;
import com.example.travellingapp.repository.collaboration.TripShareCodeRepository;
import com.example.travellingapp.service.TripShareCodeSecurityEventService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Log4j2
public class TripShareCodeSecurityEventServiceImpl
        implements TripShareCodeSecurityEventService {

    private static final int MAX_INVALID_ATTEMPTS = 5;
    private static final long RESTRICTION_MINUTES = 10;

    private final EntityManager entityManager;
    private final TripShareCodeAttemptRepository attemptRepository;
    private final TripShareCodeRepository shareCodeRepository;

    public TripShareCodeSecurityEventServiceImpl(
            EntityManager entityManager,
            TripShareCodeAttemptRepository attemptRepository,
            TripShareCodeRepository shareCodeRepository
    ) {
        this.entityManager = entityManager;
        this.attemptRepository = attemptRepository;
        this.shareCodeRepository = shareCodeRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void recordInvalidAttempt(long userId) {
        recordInvalidAttemptInCurrentTransaction(userId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void recordExpiredCodeAndInvalidAttempt(
            Long shareCodeId,
            long userId
    ) {
        shareCodeRepository.findById(shareCodeId)
                .ifPresent(shareCode -> {
                    if (shareCode.getCodeStatus() == TripEnum.ACTIVE) {
                        shareCode.setCodeStatus(TripEnum.EXPIRED);
                        shareCode.setModifiedDate(LocalDateTime.now());
                        shareCodeRepository.save(shareCode);
                    }
                });

        recordInvalidAttemptInCurrentTransaction(userId);
    }

    private void recordInvalidAttemptInCurrentTransaction(long userId) {
        User user = entityManager.find(
                User.class,
                userId,
                LockModeType.PESSIMISTIC_WRITE
        );

        if (user == null) {
            throw new IllegalStateException(
                    "Cannot record share-code attempt for missing user "
                            + userId
            );
        }

        LocalDateTime now = LocalDateTime.now();

        TripShareCodeAttemptEntity attempt = attemptRepository
                .findByUser_UserId(userId)
                .orElseGet(
                        () -> new TripShareCodeAttemptEntity(
                                user,
                                0,
                                now
                        )
                );

        if (attempt.getRestrictedUntil() != null
                && !attempt.getRestrictedUntil().isAfter(now)) {
            attempt.setRetryCount(0);
            attempt.setRestrictedUntil(null);
        }

        int newRetryCount = attempt.getRetryCount() + 1;
        attempt.setRetryCount(newRetryCount);
        attempt.setLastAttemptDate(now);
        attempt.setModifiedDate(now);

        if (newRetryCount >= MAX_INVALID_ATTEMPTS) {
            attempt.setRetryCount(0);
            attempt.setRestrictedUntil(
                    now.plusMinutes(RESTRICTION_MINUTES)
            );
        }

        attemptRepository.saveAndFlush(attempt);

        log.warn(
                "Recorded an invalid share-code attempt for user {}",
                userId
        );
    }
}