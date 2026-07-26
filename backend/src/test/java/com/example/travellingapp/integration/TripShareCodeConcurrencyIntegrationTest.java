package com.example.travellingapp.integration;

import com.example.travellingapp.entity.TripEntity;
import com.example.travellingapp.entity.User;
import com.example.travellingapp.entity.collaboration.TripShareCodeEntity;
import com.example.travellingapp.enums.TripEnum;
import com.example.travellingapp.repository.TripRepository;
import com.example.travellingapp.repository.UserRepository;
import com.example.travellingapp.repository.collaboration.TripShareCodeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class TripShareCodeConcurrencyIntegrationTest {

    private static final String FIRST_CODE = "WM-ABCDEFGHJKLM";
    private static final String SECOND_CODE = "WM-NPQRSTUVWXYZ";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private TripShareCodeRepository tripShareCodeRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private ExecutorService executorService;
    private User owner;
    private TripEntity trip;

    @BeforeEach
    void setUp() {
        tripShareCodeRepository.deleteAll();
        tripRepository.deleteAll();
        userRepository.deleteAll();

        owner = userRepository.saveAndFlush(activeUser());
        trip = tripRepository.saveAndFlush(trip(owner));
        executorService = Executors.newFixedThreadPool(2);
    }

    @AfterEach
    void tearDown() {
        executorService.shutdownNow();
    }

    @Test
    void concurrentGenerationForSameTrip_shouldCreateOnlyOneActiveCode() throws Exception {
        CountDownLatch firstTransactionLockedTrip = new CountDownLatch(1);
        CountDownLatch secondTransactionAttemptingLock = new CountDownLatch(1);
        CountDownLatch releaseFirstTransaction = new CountDownLatch(1);

        Future<Boolean> firstResult = executorService.submit(() ->
                executeInTransaction(() -> {
                    TripEntity lockedTrip = tripRepository
                            .findByTripIdForUpdate(trip.getTripId())
                            .orElseThrow();

                    firstTransactionLockedTrip.countDown();
                    await(releaseFirstTransaction);

                    boolean created = tripShareCodeRepository
                            .findFirstByTrip_TripIdAndCodeStatusOrderByCreatedDateDesc(
                                    lockedTrip.getTripId(),
                                    TripEnum.ACTIVE
                            )
                            .isEmpty();
                    if (created) {
                        tripShareCodeRepository.saveAndFlush(
                                activeShareCode(lockedTrip, FIRST_CODE)
                        );
                    }
                    return created;
                })
        );

        assertThat(firstTransactionLockedTrip.await(5, TimeUnit.SECONDS))
                .isTrue();

        Future<Boolean> secondResult = executorService.submit(() ->
                executeInTransaction(() -> {
                    secondTransactionAttemptingLock.countDown();
                    TripEntity lockedTrip = tripRepository
                            .findByTripIdForUpdate(trip.getTripId())
                            .orElseThrow();

                    boolean created = tripShareCodeRepository
                            .findFirstByTrip_TripIdAndCodeStatusOrderByCreatedDateDesc(
                                    lockedTrip.getTripId(),
                                    TripEnum.ACTIVE
                            )
                            .isEmpty();
                    if (created) {
                        tripShareCodeRepository.saveAndFlush(
                                activeShareCode(lockedTrip, SECOND_CODE)
                        );
                    }
                    return created;
                })
        );

        assertThat(secondTransactionAttemptingLock.await(5, TimeUnit.SECONDS))
                .isTrue();

        releaseFirstTransaction.countDown();

        assertThat(firstResult.get(5, TimeUnit.SECONDS)).isTrue();
        assertThat(secondResult.get(5, TimeUnit.SECONDS)).isFalse();
        assertThat(tripShareCodeRepository.findByTrip_TripIdAndCodeStatus(
                trip.getTripId(),
                TripEnum.ACTIVE
        )).hasSize(1);
    }

    @Test
    void concurrentRedemptionOfSameCode_shouldAllowOnlyOneRedemption() throws Exception {
        TripShareCodeEntity shareCode = tripShareCodeRepository.saveAndFlush(
                activeShareCode(trip, FIRST_CODE)
        );

        CountDownLatch firstTransactionLockedCode = new CountDownLatch(1);
        CountDownLatch secondTransactionAttemptingLock = new CountDownLatch(1);
        CountDownLatch releaseFirstTransaction = new CountDownLatch(1);

        Future<Boolean> firstResult = executorService.submit(() ->
                executeInTransaction(() -> {
                    TripShareCodeEntity lockedCode = tripShareCodeRepository
                            .findByCodeForUpdate(shareCode.getCode())
                            .orElseThrow();

                    firstTransactionLockedCode.countDown();
                    await(releaseFirstTransaction);

                    boolean accepted = lockedCode.getCodeStatus()
                            == TripEnum.ACTIVE;
                    if (accepted) {
                        markUsed(lockedCode);
                    }
                    return accepted;
                })
        );

        assertThat(firstTransactionLockedCode.await(5, TimeUnit.SECONDS))
                .isTrue();

        Future<Boolean> secondResult = executorService.submit(() ->
                executeInTransaction(() -> {
                    secondTransactionAttemptingLock.countDown();
                    TripShareCodeEntity lockedCode = tripShareCodeRepository
                            .findByCodeForUpdate(shareCode.getCode())
                            .orElseThrow();

                    boolean accepted = lockedCode.getCodeStatus()
                            == TripEnum.ACTIVE;
                    if (accepted) {
                        markUsed(lockedCode);
                    }
                    return accepted;
                })
        );

        assertThat(secondTransactionAttemptingLock.await(5, TimeUnit.SECONDS))
                .isTrue();

        releaseFirstTransaction.countDown();

        assertThat(firstResult.get(5, TimeUnit.SECONDS)).isTrue();
        assertThat(secondResult.get(5, TimeUnit.SECONDS)).isFalse();
        assertThat(tripShareCodeRepository.findByCode(FIRST_CODE)
                .orElseThrow()
                .getCodeStatus()).isEqualTo(TripEnum.USED);
    }

    private Boolean executeInTransaction(TransactionOperation operation) {
        TransactionTemplate transactionTemplate =
                new TransactionTemplate(transactionManager);
        return transactionTemplate.execute(status -> operation.execute());
    }

    private void markUsed(TripShareCodeEntity shareCode) {
        LocalDateTime now = LocalDateTime.now();
        shareCode.setCodeStatus(TripEnum.USED);
        shareCode.setUsedByUser(shareCode.getCreatedByUser());
        shareCode.setUsedDate(now);
        shareCode.setModifiedDate(now);
        tripShareCodeRepository.saveAndFlush(shareCode);
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException(
                        "Timed out waiting for concurrent share-code test"
                );
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Concurrent share-code test was interrupted",
                    exception
            );
        }
    }

    private User activeUser() {
        return new User(
                "share-code-owner",
                "encoded-password",
                "0412345678",
                LocalDate.of(1995, 1, 1),
                LocalDateTime.now(),
                "share-code-owner@example.com",
                true
        );
    }

    private TripEntity trip(User user) {
        return new TripEntity(
                "Concurrency trip",
                "Adelaide",
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                user
        );
    }

    private TripShareCodeEntity activeShareCode(
            TripEntity tripEntity,
            String code
    ) {
        return new TripShareCodeEntity(
                tripEntity,
                code,
                owner,
                TripEnum.VIEWER,
                TripEnum.ACTIVE,
                LocalDateTime.now().plusHours(24),
                LocalDateTime.now()
        );
    }

    @FunctionalInterface
    private interface TransactionOperation {
        Boolean execute();
    }
}
