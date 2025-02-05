package org.team12.ticketing.concurrency.concert;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.team12.ticketing.concurrency.concert.dto.ConcertRequestDto;
import org.team12.ticketing.concurrency.concert.service.ConcertService;

@SpringBootTest
public class RedisRockTest {

    private static final int THREAD_COUNT = 200;  // 동시 요청 스레드 개수
    private static final int EXPECTED_MAX_SUCCESS_COUNT = 100; // 최대 티켓 수 (성공 건수)

    @Autowired
    private ConcertService concertService;

    @Test
    public void testTicketPurchaseWithConcurrency() throws InterruptedException, ExecutionException {

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CyclicBarrier barrier = new CyclicBarrier(THREAD_COUNT); // 모든 스레드가 동시에 실행되도록 조정
        List<Future<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < THREAD_COUNT; i++) {
            futures.add(executorService.submit(() -> {
                try {
                    barrier.await();  // 모든 스레드가 준비될 때까지 대기
                    return concertService.buyTicket(1L,
                        new ConcertRequestDto(
                            Thread.currentThread().getId(),  // userId
                            "Test Concert",                  // title
                            "Test Singer",                   // singer
                            "Test Content",                   // content
                            1L                                // ticketAmount (1장 구매)
                        )
                    ) != null;
                } catch (Exception e) {
                    return false; // 예외 발생 시 실패 처리
                }
            }));
        }

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS); // 10초 동안 모든 스레드 종료 대기

        int successCount = 0;
        for (Future<Boolean> future : futures) {
            if (future.get()) {
                successCount++;
            }
        }

        // 구매 성공 건수는 최대 100건을 초과하면 안됨
        assertTrue(successCount <= EXPECTED_MAX_SUCCESS_COUNT, "티켓 구매 성공 건수가 초과되었습니다. 현재: " + successCount);
    }
}
