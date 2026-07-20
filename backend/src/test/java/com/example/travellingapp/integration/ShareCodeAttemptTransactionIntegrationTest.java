package com.example.travellingapp.integration;

import com.example.travellingapp.entity.User;
import com.example.travellingapp.entity.collaboration.TripShareCodeAttemptEntity;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.repository.UserRepository;
import com.example.travellingapp.repository.collaboration.TripShareCodeAttemptRepository;
import com.example.travellingapp.service.TripShareCodeSecurityEventService;
import com.example.travellingapp.service.impl.TripShareCodeSecurityEventServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.example.travellingapp.enums.CommonEnum.TRIP_MEMBER;
import static com.example.travellingapp.enums.ErrorCodeEnum.TRIP_SHARE_CODE_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@Import({
        TripShareCodeSecurityEventServiceImpl.class,
        ShareCodeAttemptTransactionIntegrationTest.FailingJoinOperation.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ShareCodeAttemptTransactionIntegrationTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TripShareCodeAttemptRepository attemptRepository;

    @Autowired
    private FailingJoinOperation failingJoinOperation;

    private User user;

    @BeforeEach
    void setUp() {
        attemptRepository.deleteAll();
        userRepository.deleteAll();
        user = userRepository.saveAndFlush(activeUser());
    }

    @Test
    void invalidShareCodeAttempt_remainsCommittedAfterJoinTransactionRollsBack() {
        assertThrows(
                BusinessException.class,
                () -> failingJoinOperation.recordThenFail(user.getUserId())
        );

        TripShareCodeAttemptEntity persisted = attemptRepository
                .findByUser_UserId(user.getUserId())
                .orElseThrow();

        assertThat(persisted.getRetryCount()).isEqualTo(1);
        assertThat(persisted.getLastAttemptDate()).isNotNull();
    }

    @Test
    void fifthInvalidAttempt_commitsTemporaryRestrictionDespiteOuterRollbacks() {
        for (int attempt = 0; attempt < 5; attempt++) {
            assertThrows(
                    BusinessException.class,
                    () -> failingJoinOperation.recordThenFail(user.getUserId())
            );
        }

        TripShareCodeAttemptEntity persisted = attemptRepository
                .findByUser_UserId(user.getUserId())
                .orElseThrow();

        assertThat(persisted.getRetryCount()).isZero();
        assertThat(persisted.getRestrictedUntil()).isAfter(LocalDateTime.now());
    }

    private User activeUser() {
        return new User(
                "share-code-user",
                "encoded-password",
                "0412345678",
                LocalDate.of(1995, 1, 1),
                LocalDateTime.now(),
                "share-code@example.com",
                true
        );
    }

    @Service
    static class FailingJoinOperation {
        private final TripShareCodeSecurityEventService securityEventService;

        FailingJoinOperation(TripShareCodeSecurityEventService securityEventService) {
            this.securityEventService = securityEventService;
        }

        @Transactional
        public void recordThenFail(long userId) {
            securityEventService.recordInvalidAttempt(userId);
            throw new BusinessException(TRIP_SHARE_CODE_NOT_FOUND, TRIP_MEMBER.name());
        }
    }
}
