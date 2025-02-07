package org.team12.ticketing.concurrency.booking.service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.team12.ticketing.concurrency.booking.domain.Booking;
import org.team12.ticketing.concurrency.booking.dto.BookingRequestDto;
import org.team12.ticketing.concurrency.booking.dto.BookingResponseDto;
import org.team12.ticketing.concurrency.booking.repository.BookingRepository;
import org.team12.ticketing.concurrency.common.RedisLockUtil;
import org.team12.ticketing.concurrency.concert.domain.Concert;
import org.team12.ticketing.concurrency.concert.repository.ConcertRepository;

import java.util.List;
import org.team12.ticketing.concurrency.lock.RedisRockRepository;


@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ConcertRepository concertRepository;
    private final RedisRockRepository redisRockRepository;


    public BookingResponseDto bookingTicket(Long concertId, BookingRequestDto dto) {
        String lockValue = UUID.randomUUID().toString();
        log.info("Attempting to acquire Redis lock with value: {}", lockValue);

        int retryCount = 100;
        long retryDelayMillis = 200;
        boolean acquired = false;

        for (int i = 0; i < retryCount; i++) {
            acquired = redisRockRepository.acquireLock(
                RedisLockUtil.CONCERT_TICKET_LOCK_KEY,
                lockValue,
                RedisLockUtil.CONCERT_LOCK_TIMEOUT_MS
            );
            if (acquired) {
                log.info("Lock acquired on attempt {}", i + 1);
                log.info("user id: {}", dto.getUserId());
                break;
            }
            try {
                log.info("Lock not acquired on attempt {}. Retrying in {} ms", i + 1, retryDelayMillis);
                log.info("user id: {}", dto.getUserId());
                TimeUnit.MILLISECONDS.sleep(retryDelayMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Interrupted during lock retry", e);
                throw new RuntimeException("Lock 재시도 중 인터럽트 발생", e);
            }
        }

        if (!acquired) {
            log.error("Failed to acquire lock after {} attempts", retryCount);
            throw new RuntimeException("동시 구매 요청이 너무 많습니다. 잠시 후 다시 시도해주세요.");
        }

        try {
            // 트랜잭션을 락 안에서 실행
            return executeBooking(concertId, dto);
        } catch (Exception e) {
                log.error("Exception during booking: ", e);
                throw e; // 예외를 다시 던져서 트랜잭션 롤백
        } finally {
            // 트랜잭션이 끝난 후에 락을 해제
            log.info("Releasing Redis lock with value: {}", lockValue);
            redisRockRepository.releaseLock(RedisLockUtil.CONCERT_TICKET_LOCK_KEY, lockValue);
            boolean isLocked = redisRockRepository.isLocked(RedisLockUtil.CONCERT_TICKET_LOCK_KEY);
            log.info("Lock status: {}", isLocked ? "LOCKED" : "UNLOCKED");
        }
    }

    @Transactional
    public BookingResponseDto executeBooking(Long concertId, BookingRequestDto dto) {
        log.info("Fetching concert with id: {}", concertId);
        Concert concert = concertRepository.findById(concertId)
            .orElseThrow(() -> new IllegalArgumentException("Concert not found"));

        log.info("Decreasing remaining ticket amount for concert id: {}", concertId);
        concert.decreaseRemainTicketAmount();
        concertRepository.save(concert);

        log.info("Creating booking for user id: {}", dto.getUserId());
        Booking booking = Booking.builder()
            .concert(concert)
            .userId(dto.getUserId())
            .build();
        Booking savedBooking = bookingRepository.save(booking);

        return BookingResponseDto.builder()
            .concertId(savedBooking.getConcert().getId())
            .bookingOrder(savedBooking.getBookingOrder())
            .build();
    }

    public List<BookingResponseDto> findTicket(Long concertId, Long userId) {
        List<Booking> foundBookingList = bookingRepository.findByConcert_IdAndUserId(concertId, userId);

        return foundBookingList.stream()
                .map(booking -> BookingResponseDto.builder()
                        .concertId(booking.getConcert().getId())
                        .bookingOrder(booking.getBookingOrder())
                        .build())
                .toList();
    }
}
