package com.example.travellingapp.integration;

import com.example.travellingapp.entity.RefreshTokenEntity;
import com.example.travellingapp.repository.RefreshTokenRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class RefreshTokenRotationConcurrencyIntegrationTest {

    private static final String TOKEN_HASH = "concurrent-refresh-token-hash";

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        refreshTokenRepository.saveAndFlush(activeRefreshToken());
        executorService = Executors.newFixedThreadPool(2);
    }

    @AfterEach
    void tearDown() {
        executorService.shutdownNow();
    }

    @Test
    void concurrentConsumersOfSameRefreshToken_shouldAllowOnlyOneConsumer() throws Exception {
        CountDownLatch firstTransactionLockedToken = new CountDownLatch(1);
        CountDownLatch secondTransactionAttemptingLock = new CountDownLatch(1);
        CountDownLatch releaseFirstTransaction = new CountDownLatch(1);

        Future<Boolean> firstResult = executorService.submit(() ->
                executeInTransaction(() -> {
                    RefreshTokenEntity token = refreshTokenRepository
                            .findByTokenHashForUpdate(TOKEN_HASH)
                            .orElseThrow();

                    firstTransactionLockedToken.countDown();
                    await(releaseFirstTransaction);

                    boolean accepted = !token.isRevoked();
                    if (accepted) {
                        token.setRevoked(true);
                        token.setModifiedDate(LocalDateTime.now());
                        refreshTokenRepository.saveAndFlush(token);
                    }
                    return accepted;
                })
        );

        assertThat(firstTransactionLockedToken.await(5, TimeUnit.SECONDS))
                .isTrue();

        Future<Boolean> secondResult = executorService.submit(() ->
                executeInTransaction(() -> {
                    secondTransactionAttemptingLock.countDown();
                    RefreshTokenEntity token = refreshTokenRepository
                            .findByTokenHashForUpdate(TOKEN_HASH)
                            .orElseThrow();

                    boolean accepted = !token.isRevoked();
                    if (accepted) {
                        token.setRevoked(true);
                        token.setModifiedDate(LocalDateTime.now());
                        refreshTokenRepository.saveAndFlush(token);
                    }
                    return accepted;
                })
        );

        assertThat(secondTransactionAttemptingLock.await(5, TimeUnit.SECONDS))
                .isTrue();

        releaseFirstTransaction.countDown();

        assertThat(firstResult.get(5, TimeUnit.SECONDS))
                .isTrue();
        assertThat(secondResult.get(5, TimeUnit.SECONDS))
                .isFalse();
    }

    private Boolean executeInTransaction(TransactionOperation operation) {
        TransactionTemplate transactionTemplate =
                new TransactionTemplate(transactionManager);
        return transactionTemplate.execute(status -> operation.execute());
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for concurrent refresh-token test");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Concurrent refresh-token test was interrupted", e);
        }
    }

    private static RefreshTokenEntity activeRefreshToken() {
        return new RefreshTokenEntity(
                false,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1),
                "JustinBo123",
                TOKEN_HASH,
                "session-1",
                null
        );
    }

    @FunctionalInterface
    private interface TransactionOperation {
        Boolean execute();
    }
}
