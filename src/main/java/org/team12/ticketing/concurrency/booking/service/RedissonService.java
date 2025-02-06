package org.team12.ticketing.concurrency.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.team12.ticketing.concurrency.booking.dto.BookingRequestDto;
import org.team12.ticketing.concurrency.booking.dto.BookingResponseDto;

import java.util.concurrent.TimeUnit;

//AOP 구현 전 코드
@Service
@Slf4j
@RequiredArgsConstructor
public class RedissonService {
    private final RedissonClient redissonClient;
    private final BookingService bookingService;


    public BookingResponseDto executeWithLock(Long concertId, BookingRequestDto dto) {
        String lockKey = "concert_lock_" + concertId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            log.info("락 획득 시도: userId = {}", dto.getUserId());
            if (lock.tryLock(10, 3, TimeUnit.SECONDS)) {
                try {
                    log.info("락 획득 성공: userId = {}", dto.getUserId());
                    return bookingService.bookWithRedissonLock(concertId, dto);

                } catch (Exception e) {
                    RuntimeException noStackTraceException = new RuntimeException("예매 실패: " + e.getMessage());
                    noStackTraceException.setStackTrace(new StackTraceElement[0]); // 스택 트레이스 제거
                    throw noStackTraceException;
                } finally {
                    if (lock.isHeldByCurrentThread()) {
                        log.info("락 해제: userId = {}", dto.getUserId());
                        lock.unlock();
                    }
                }
            } else {
                log.info("락 획득 실패: userId = {}", dto.getUserId());
                throw new RuntimeException("현재 이용자가 많아 대기 중입니다. 잠시 후 다시 시도하세요.");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("예매 처리 중 오류 발생", e);
        }
    }
}