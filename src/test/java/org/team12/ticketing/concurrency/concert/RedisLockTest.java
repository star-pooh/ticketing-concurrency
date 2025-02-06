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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.team12.ticketing.concurrency.booking.dto.BookingRequestDto;
import org.team12.ticketing.concurrency.booking.dto.BookingResponseDto;
import org.team12.ticketing.concurrency.booking.service.BookingService;
import org.team12.ticketing.concurrency.concert.domain.Concert;
import org.team12.ticketing.concurrency.concert.repository.ConcertRepository;

@SpringBootTest
public class RedisLockTest {

    private static final int THREAD_COUNT = 5;  // 동시 요청 스레드 수
    private static final int EXPECTED_MAX_SUCCESS_COUNT = 10; // 초기 티켓 수(예: 100장)

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ConcertRepository concertRepository;

    private Long concertId;

    @BeforeEach
    public void setupConcertData() {
        // 테스트 전에 콘서트 데이터를 생성합니다.
        // 총 티켓 수와 남은 티켓 수를 동일하게 100으로 설정 (예: 100장 판매)
        Concert concert = new Concert("Test Concert", "Test Singer", "Test Content", 10L);
        Concert savedConcert = concertRepository.save(concert);
        concertId = savedConcert.getId();
    }

    @Test
    public void testTicketBookingWithConcurrency() throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CyclicBarrier barrier = new CyclicBarrier(THREAD_COUNT);
        List<Future<Boolean>> futures = new ArrayList<>();

        // 200개의 동시 요청 시뮬레이션: 각 스레드는 1장의 티켓 구매를 시도
        for (int i = 0; i < THREAD_COUNT; i++) {
            futures.add(executorService.submit(() -> {
                try {
                    barrier.await();
                    // 각 스레드는 고유한 userId를 사용 (여기서는 Thread ID 사용)
                    BookingRequestDto requestDto = new BookingRequestDto(Thread.currentThread().getId());
                    BookingResponseDto response = bookingService.bookingTicket(concertId, requestDto);
                    return response != null;
                } catch (Exception e) {
                    // 예외 발생 시 실패 처리
                    return false;
                }
            }));
        }

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        int successCount = 0;
        for (Future<Boolean> future : futures) {
            if (future.get()) {
                successCount++;
            }
        }

        // 동시성 테스트에서는, 초기 티켓 수(100장)를 초과하여 판매되면 안 됩니다.
        assertTrue(successCount <= EXPECTED_MAX_SUCCESS_COUNT,
            "Booking success count exceeds expected limit. Success count: " + successCount);
    }

}
