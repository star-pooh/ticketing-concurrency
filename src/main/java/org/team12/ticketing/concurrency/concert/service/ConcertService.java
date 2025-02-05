package org.team12.ticketing.concurrency.concert.service;

import jakarta.transaction.Transactional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.team12.ticketing.concurrency.common.RedisLockUtil;
import org.team12.ticketing.concurrency.concert.domain.Concert;
import org.team12.ticketing.concurrency.concert.dto.ConcertRequestDto;
import org.team12.ticketing.concurrency.concert.dto.ConcertResponseDto;
import org.team12.ticketing.concurrency.concert.repository.ConcertRepository;
import org.team12.ticketing.concurrency.lock.LockRedisRepository;
import org.team12.ticketing.concurrency.user.domain.User;
import org.team12.ticketing.concurrency.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class ConcertService {

    private final ConcertRepository concertRepository;
    private final UserRepository userRepository;
    private final LockRedisRepository lockRedisRepository;

    @Transactional
    public ConcertResponseDto updateConcert(Long concertId, ConcertRequestDto dto) {
        Concert foundedConcert = concertRepository.findById(concertId)
            .orElseThrow(() -> new IllegalArgumentException("concert not found"));
        foundedConcert.update(dto);

        return ConcertResponseDto.of(foundedConcert);
    }

    @Transactional
    public ConcertResponseDto buyTicket(Long concertId, ConcertRequestDto requestDto) {
        // 1. 사용자 조회 (티켓 구매 요청을 보낸 사용자 정보 확인)
        User user = userRepository.findById(requestDto.getUserId())
            .orElseThrow(() -> new RuntimeException("해당 유저가 존재하지 않습니다."));

        // 2. 콘서트 조회 (티켓 구매 대상 콘서트 정보 확인)
        Concert concert = concertRepository.findById(concertId)
            .orElseThrow(() -> new RuntimeException("해당 콘서트가 존재하지 않습니다."));

        // 3. Redis Lock 획득: 동시에 여러 사용자가 구매 요청 시 동시성 문제를 방지하기 위해 Lock 적용
        //    - 공통 유틸 클래스(RedisLockUtil)에서 Lock Key와 Timeout 상수를 관리함
        //    - 고유한 lockValue를 생성하여 해당 요청의 소유권을 표시
        String lockValue = UUID.randomUUID().toString();
        int retryCount = 3;           // 재시도 횟수
        long retryDelayMillis = 100;    // 재시도 간격 (100ms)
        boolean acquired = false;

        // 재시도 로직: 설정한 횟수만큼 Lock 획득 시도
        for (int i = 0; i < retryCount; i++) {
            acquired = lockRedisRepository.acquireLock(
                RedisLockUtil.CONCERT_TICKET_LOCK_KEY,
                lockValue,
                RedisLockUtil.CONCERT_LOCK_TIMEOUT_MS
            );
            if (acquired) {
                break;
            }
            try {
                Thread.sleep(retryDelayMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Lock 재시도 중 인터럽트가 발생했습니다.", e);
            }
        }

        if (!acquired) {
            // Lock 획득에 실패하면, 동시 구매 요청이 많다는 의미이므로 예외 처리
            throw new RuntimeException("동시 구매 요청이 너무 많습니다. 잠시 후 다시 시도해주세요.");
        }

        try {
            // 4. 티켓 구매 로직 실행 (Lock이 확보된 상태에서 실행)
            //    - 현재 남은 티켓 갯수를 ticketNumber로 저장 (동시성 이슈 발생 가능성이 있는 부분)
            Long ticketNumber = concert.getTicketAmount();

            //    - 티켓 재고 감소: 남은 티켓 갯수를 1 감소
            concert.decreaseTicketAmount();
            concertRepository.save(concert);

            //    - 티켓 번호를 사용자에게 할당: 구매한 티켓 번호를 사용자 정보에 저장
            user.setTicketInfo(concert, ticketNumber);
            userRepository.save(user);

            // 5. 결과 반환: 최신 콘서트 정보를 바탕으로 ConcertResponseDto 생성 후 반환
            return ConcertResponseDto.of(concert);
        } finally {
            // 6. Lock 해제: 반드시 Lock을 해제하여 다른 요청이 진행될 수 있도록 함
            lockRedisRepository.releaseLock(RedisLockUtil.CONCERT_TICKET_LOCK_KEY, lockValue);
        }
    }
}
