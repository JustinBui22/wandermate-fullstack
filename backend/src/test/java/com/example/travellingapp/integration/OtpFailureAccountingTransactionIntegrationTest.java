package com.example.travellingapp.integration;

import com.example.travellingapp.entity.OtpCheckEntity;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.repository.OtpCheckRepository;
import com.example.travellingapp.service.OtpFailureAccountingService;
import com.example.travellingapp.service.impl.OtpFailureAccountingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.example.travellingapp.enums.CommonEnum.OTP;
import static com.example.travellingapp.enums.ErrorCodeEnum.OTP_CODE_NOT_CORRECT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@Import({
        OtpFailureAccountingServiceImpl.class,
        OtpFailureAccountingTransactionIntegrationTest.FailingOtpOperation.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class OtpFailureAccountingTransactionIntegrationTest {
    @Autowired
    private OtpCheckRepository otpCheckRepository;

    @Autowired
    private FailingOtpOperation failingOtpOperation;

    @BeforeEach
    void setUp() {
        otpCheckRepository.deleteAll();
    }

    @Test
    void failedOtpAttempt_remainsCommittedAfterOuterBusinessExceptionRollsBack() {
        OtpCheckEntity otpCheck = otpCheckRepository.saveAndFlush(otpRecord(0));

        assertThrows(
                BusinessException.class,
                () -> failingOtpOperation.recordThenFail(otpCheck.getOtpCheckId())
        );

        OtpCheckEntity persisted = otpCheckRepository.findById(otpCheck.getOtpCheckId())
                .orElseThrow();

        assertThat(persisted.getRetryVerifyOtpCount()).isEqualTo(1);
        assertThat(persisted.isBlock()).isFalse();
    }

    @Test
    void failedOtpAttempt_blocksRecordAtConfiguredLimitDespiteOuterRollback() {
        OtpCheckEntity otpCheck = otpCheckRepository.saveAndFlush(otpRecord(2));

        assertThrows(
                BusinessException.class,
                () -> failingOtpOperation.recordThenFail(otpCheck.getOtpCheckId())
        );

        OtpCheckEntity persisted = otpCheckRepository.findById(otpCheck.getOtpCheckId())
                .orElseThrow();

        assertThat(persisted.getRetryVerifyOtpCount()).isEqualTo(3);
        assertThat(persisted.isBlock()).isTrue();
        assertThat(persisted.getOtpRestrictedTime()).isAfter(Instant.now());
    }

    private OtpCheckEntity otpRecord(int retryCount) {
        OtpCheckEntity entity = new OtpCheckEntity();
        entity.setUsername("otp-transaction-user");
        entity.setEmail("otp-transaction@example.com");
        entity.setCreatedDate(Instant.now());
        entity.setRetrySendOtpCount(1);
        entity.setRetryVerifyOtpCount(retryCount);
        entity.setNewestOtp("123456");
        entity.setBlock(false);
        entity.setOtpExpirationTime(Instant.now().plus(2, ChronoUnit.MINUTES));
        return entity;
    }

    @Service
    static class FailingOtpOperation {
        private final OtpFailureAccountingService failureAccountingService;

        FailingOtpOperation(OtpFailureAccountingService failureAccountingService) {
            this.failureAccountingService = failureAccountingService;
        }

        @Transactional
        public void recordThenFail(int otpCheckId) {
            failureAccountingService.recordFailedVerification(
                    otpCheckId,
                    3,
                    900_000L
            );
            throw new BusinessException(OTP_CODE_NOT_CORRECT, OTP.name());
        }
    }
}
