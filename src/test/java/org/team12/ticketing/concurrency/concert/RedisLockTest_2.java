package org.team12.ticketing.concurrency.concert;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.team12.ticketing.concurrency.lock.RedisRockRepository;

@SpringBootTest
public class RedisLockTest_2 {

    private static final Logger log = LoggerFactory.getLogger(RedisLockTest_2.class);

    private static final int THREAD_COUNT = 5;  // 동시 요청 스레드 수

    // RedisRockRepository 빈 주입 (Lettuce 기반 락 서비스)
    @Autowired
    private RedisRockRepository redisRockRepository;

    @Test
    @DisplayName("Lettuce 기반 분산 락 테스트")
    void concurrentLockTest() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        CyclicBarrier barrier = new CyclicBarrier(THREAD_COUNT);
        AtomicInteger successCount = new AtomicInteger(0);
        ConcurrentLinkedQueue<Long> failedUserIds = new ConcurrentLinkedQueue<>();

        for (int i = 0; i < THREAD_COUNT; i++) {
            final long userId = i + 1;
            executorService.submit(() -> {
                try {
                    barrier.await(); // 모든 스레드가 동시에 시작되도록 동기화
                    String lockKey = "concert_lock";
                    String lockValue = UUID.randomUUID().toString();

                    log.info(() -> "User " + userId + " attempting to acquire lock with value: " + lockValue);
                    // TTL을 10초(10000ms)로 설정
                    boolean isLocked = redisRockRepository.acquireLock(lockKey, lockValue, 10000);

                    if (isLocked) {
                        log.info(() -> "User " + userId + " successfully acquired lock.");
                        try {
                            // 임의의 작업: 50ms 대기 (실제 처리 시뮬레이션)
                            Thread.sleep(50);
                            successCount.incrementAndGet();
                        } finally {
                            redisRockRepository.releaseLock(lockKey, lockValue);
                            log.info(() -> "User " + userId + " released lock with value: " + lockValue);
                        }
                    } else {
                        log.info(() -> "User " + userId + " failed to acquire lock.");
                        failedUserIds.add(userId);
                    }
                } catch (Exception e) {
                    log.error(() -> "Error for user " + userId + ": " + e.getMessage());
                    failedUserIds.add(userId);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드 종료 대기
        executorService.shutdown();

        log.info(() -> "All threads finished. Failed user IDs: " + failedUserIds);

        // 단일 락이므로, 동시에 락을 획득한 사용자는 최대 1명이어야 합니다.
        assertThat(successCount.get()).isLessThanOrEqualTo(1);
    }
}
