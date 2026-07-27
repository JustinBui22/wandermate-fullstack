package com.example.travellingapp.service.impl;

import com.example.travellingapp.entity.OtpCheckEntity;
import com.example.travellingapp.repository.OtpCheckRepository;
import com.example.travellingapp.service.OtpFailureAccountingService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
@Log4j2
public class OtpFailureAccountingServiceImpl
        implements OtpFailureAccountingService {

    private final EntityManager entityManager;
    private final OtpCheckRepository otpCheckRepository;

    public OtpFailureAccountingServiceImpl(
            EntityManager entityManager,
            OtpCheckRepository otpCheckRepository
    ) {
        this.entityManager = entityManager;
        this.otpCheckRepository = otpCheckRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void recordFailedVerification(
            int otpCheckId,
            int maxRetryVerifyOtp,
            long restrictedDurationMillis
    ) {
        OtpCheckEntity otpCheck = entityManager.find(
                OtpCheckEntity.class,
                otpCheckId,
                LockModeType.PESSIMISTIC_WRITE
        );

        if (otpCheck == null) {
            log.warn(
                    "OTP record {} disappeared before its failed attempt could be recorded",
                    otpCheckId
            );
            return;
        }

        int newRetryCount = otpCheck.getRetryVerifyOtpCount() + 1;
        otpCheck.setRetryVerifyOtpCount(newRetryCount);

        if (newRetryCount >= maxRetryVerifyOtp) {
            otpCheck.setBlock(true);
            otpCheck.setOtpRestrictedTime(
                    Instant.now().plus(
                            Duration.ofMillis(restrictedDurationMillis)
                    )
            );
        }

        otpCheckRepository.saveAndFlush(otpCheck);
    }
}