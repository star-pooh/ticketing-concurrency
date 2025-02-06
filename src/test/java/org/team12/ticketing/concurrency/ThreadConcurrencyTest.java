package org.team12.ticketing.concurrency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.team12.ticketing.concurrency.booking.dto.BookingRequestDto;
import org.team12.ticketing.concurrency.booking.service.BookingSynchronizedService;
import org.team12.ticketing.concurrency.concert.domain.Concert;
import org.team12.ticketing.concurrency.concert.repository.ConcertRepository;

import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ThreadConcurrencyTest {

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private BookingSynchronizedService bookingSynchronizedService;

    private static final int THREAD_COUNT = 100; // 동시 요청 개수
    private static final long TEST_CONCERT_ID = 1L; // 테스트 콘서트 ID


    @BeforeEach
    void setup() {
        // 테이블 초기화
        concertRepository.save(TestData.createConcert());
    }

    @Test
    @DisplayName("synchronized를 사용하여 동시성 제어 확인")
    void threadSynchronizedTest() throws InterruptedException {
        ExecutorService executorService = new ThreadPoolExecutor(
                THREAD_COUNT,
                THREAD_COUNT,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1)
        );

        CyclicBarrier cyclicBarrier = new CyclicBarrier(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            long userId = i + 1;
            executorService.submit(() -> {
                try {
                    cyclicBarrier.await(); // 모든 스레드가 준비될 때까지 준비
                    bookingSynchronizedService.synchronizedBookTicket(TEST_CONCERT_ID, new BookingRequestDto(userId));
                } catch (Exception e) {
                    // 실패한 요청은 무시
                    System.out.println(userId + " / " + e.getMessage());
                }
            });
        }

        executorService.shutdown();
        if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
            throw new RuntimeException("스레드가 종료되지 않았습니다.");
        }

        Concert concert = concertRepository.findById(TEST_CONCERT_ID).orElseThrow();
        assertThat(concert.getRemainTicketAmount()).isEqualTo(0);
    }
}