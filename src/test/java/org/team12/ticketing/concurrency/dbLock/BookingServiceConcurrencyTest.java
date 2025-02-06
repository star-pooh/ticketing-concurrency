package org.team12.ticketing.concurrency.dbLock;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.team12.ticketing.concurrency.booking.domain.Booking;
import org.team12.ticketing.concurrency.booking.dto.BookingRequestDto;
import org.team12.ticketing.concurrency.booking.repository.BookingRepository;
import org.team12.ticketing.concurrency.booking.service.BookingService;
import org.team12.ticketing.concurrency.booking.service.OptimisticLockBookingService;
import org.team12.ticketing.concurrency.concert.domain.Concert;
import org.team12.ticketing.concurrency.concert.repository.ConcertRepository;

@SpringBootTest
public class BookingServiceConcurrencyTest {

	@Autowired
	private BookingService bookingService;

	@Autowired
	private OptimisticLockBookingService optimisticLockBookingService;

	@Autowired
	private ConcertRepository concertRepository;

	@Autowired
	private BookingRepository bookingRepository;

	// ✅ 숫자 및 문자열 상수 정리
	private static final long TEST_CONCERT_ID = 1L; // Concert ID를 저장할 변수
	private static final int THREAD_COUNT = 100; // 동시 요청 개수
	private static final long TOTAL_TICKET_AMOUNT = 100L; // 총 티켓 개수
	private static final String TEST_CONCERT_TITLE = "동시성 테스트 콘서트";
	private static final String TEST_CONCERT_SINGER = "테스트 가수";
	private static final String TEST_CONCERT_CONTENT = "테스트 콘서트 내용";

	@BeforeEach
	void setUp() {
		// // ✅ 기존 데이터를 삭제 (DB 정리)
		// bookingRepository.deleteAll();
		// concertRepository.deleteAll();

		// ✅ 테스트 실행 전 Concert 엔티티 초기화
		Concert concert = new Concert(TEST_CONCERT_TITLE, TEST_CONCERT_SINGER, TEST_CONCERT_CONTENT, TOTAL_TICKET_AMOUNT);
		concertRepository.save(concert);

	}

	@Test
	@DisplayName("CyclicBarrier를 사용하여 정확히 동시에 예매 요청을 시작하고 데이터 정합성을 확인")
	void concurrentBookingTest() throws InterruptedException {
		ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
		CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
		CyclicBarrier barrier = new CyclicBarrier(THREAD_COUNT); // ✅ 정확히 동시 시작을 보장

		AtomicInteger successCount = new AtomicInteger(0);

		for (int i = 0; i < THREAD_COUNT; i++) {
			final long userId = i + 1;
			executorService.submit(() -> {
				try {
					barrier.await(); // ✅ 모든 스레드가 준비될 때까지 대기
					BookingRequestDto request = new BookingRequestDto(userId, TEST_CONCERT_ID);
					bookingService.bookTicket(1L, request);
					successCount.incrementAndGet();
				} catch (Exception e) {
					// 실패한 요청 (티켓 초과 예매 등)은 무시
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await(); // 모든 스레드 종료 대기
		executorService.shutdown();

		assertThat(successCount.get()).isEqualTo(TOTAL_TICKET_AMOUNT);

		// ✅ 동일한 Concert ID에 대한 bookingOrder 중복 확인
		List<Long> bookingOrders = bookingRepository.findByConcertId(TEST_CONCERT_ID)
			.stream()
			.map(Booking::getBookingOrder)
			.toList();

		Set<Long> uniqueBookingOrders = Set.copyOf(bookingOrders);

		assertThat(bookingOrders.size()).isEqualTo(TOTAL_TICKET_AMOUNT);
		assertThat(uniqueBookingOrders.size()).isEqualTo(TOTAL_TICKET_AMOUNT); // 중복된 bookingOrder가 없어야 함

		// ✅ 실제 DB에서 남은 티켓 개수 확인 (Lost Update 방지)
		Concert concert = concertRepository.findById(TEST_CONCERT_ID).orElseThrow();
		assertThat(concert.getRemainTicketAmount()).isEqualTo(0);

		// ✅ 콘서트 제목이 올바르게 저장되었는지 검증
		assertThat(concert.getTitle()).isEqualTo(TEST_CONCERT_TITLE);
	}

	@Test
	@DisplayName("낙관적 락을 사용한 동시 예매 테스트")
	void concurrentBookingWithOptimisticLock() throws InterruptedException {
		// given
		ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
		CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
		CyclicBarrier barrier = new CyclicBarrier(THREAD_COUNT);
		AtomicInteger successCount = new AtomicInteger(0);

		// when
		for (int i = 0; i < THREAD_COUNT; i++) {
			final long userId = i + 1;
			executorService.submit(() -> {
				try {
					barrier.await();
					BookingRequestDto request = new BookingRequestDto(userId, TEST_CONCERT_ID);
					optimisticLockBookingService.tryBookingWithRetry(request);
					successCount.incrementAndGet();
				} catch (Exception e) {
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();
		executorService.shutdown();

		// then
		// 1. 성공한 예매가 티켓 수량과 동일한지 확인
		assertThat(successCount.get()).isEqualTo(TOTAL_TICKET_AMOUNT);

		// 2. 예매 번호 중복 없는지 확인
		List<Long> bookingOrders = bookingRepository.findByConcertId(TEST_CONCERT_ID)
			.stream()
			.map(Booking::getBookingOrder)
			.toList();
		Set<Long> uniqueBookingOrders = Set.copyOf(bookingOrders);

		assertThat(bookingOrders.size()).isEqualTo(TOTAL_TICKET_AMOUNT);
		assertThat(uniqueBookingOrders.size()).isEqualTo(TOTAL_TICKET_AMOUNT);

		// 3. 남은 티켓 수량이 0인지 확인
		Concert concert = concertRepository.findById(TEST_CONCERT_ID).orElseThrow();
		assertThat(concert.getRemainTicketAmount()).isZero();

		// 4. 콘서트 정보가 정확한지 확인
		assertThat(concert.getTitle()).isEqualTo(TEST_CONCERT_TITLE);
	}

	@Test
	@DisplayName("비관적 락을 사용한 동시 예매 테스트")
	void concurrentBookingWithPessimisticLockTest() throws InterruptedException {
		ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
		CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
		CyclicBarrier barrier = new CyclicBarrier(THREAD_COUNT);

		AtomicInteger successCount = new AtomicInteger(0);

		for (int i = 0; i < THREAD_COUNT; i++) {
			final long userId = i + 1;
			executorService.submit(() -> {
				try {
					barrier.await(); // 모든 스레드가 준비될 때까지 대기
					BookingRequestDto request = new BookingRequestDto(userId, TEST_CONCERT_ID);
					bookingService.bookTicketWithPessimisticLock(request);
					successCount.incrementAndGet();
				} catch (Exception e) {
				e.getMessage();
					// 실패한 요청 (티켓 초과 예매 등)은 무시
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await(); // 모든 스레드 종료 대기
		executorService.shutdown();

		assertThat(successCount.get()).isEqualTo(TOTAL_TICKET_AMOUNT);

		// ✅ 동일한 Concert ID에 대한 bookingOrder 중복 확인
		List<Long> bookingOrders = bookingRepository.findByConcertId(TEST_CONCERT_ID)
			.stream()
			.map(Booking::getBookingOrder)
			.toList();

		Set<Long> uniqueBookingOrders = Set.copyOf(bookingOrders);

		assertThat(bookingOrders.size()).isEqualTo(TOTAL_TICKET_AMOUNT);
		assertThat(uniqueBookingOrders.size()).isEqualTo(TOTAL_TICKET_AMOUNT); // 중복된 bookingOrder가 없어야 함

		// ✅ 실제 DB에서 남은 티켓 개수 확인
		Concert concert = concertRepository.findById(TEST_CONCERT_ID).orElseThrow();
		assertThat(concert.getRemainTicketAmount()).isEqualTo(0);
	}

	    // @AfterEach
	    // void tearDown() {
	    //     // ✅ 테스트 종료 후 데이터 정리 (DB 클리어)
	    //     bookingRepository.deleteAll();
	    //     concertRepository.deleteAll();
	    // }
}
