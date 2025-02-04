package org.team12.ticketing.concurrency.concert.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.team12.ticketing.concurrency.concert.domain.Concert;
import org.team12.ticketing.concurrency.concert.dto.ConcertRequestDto;
import org.team12.ticketing.concurrency.concert.dto.ConcertResponseDto;
import org.team12.ticketing.concurrency.concert.repository.ConcertRepository;
import org.team12.ticketing.concurrency.user.domain.User;
import org.team12.ticketing.concurrency.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class ConcertService {

    private final ConcertRepository concertRepository;
    private final UserRepository userRepository;

    @Transactional
    public ConcertResponseDto updateConcert(Long concertId, ConcertRequestDto dto) {
        Concert foundedConcert = concertRepository.findById(concertId).orElseThrow(() -> new IllegalArgumentException("concert not found"));
        foundedConcert.update(dto);

        return ConcertResponseDto.of(foundedConcert);
    }

    @Transactional
    public ResponseEntity<String> buyTicket(Long concertId, ConcertRequestDto requestDto) {
        User user = userRepository.findById(requestDto.getUserId())
            .orElseThrow(() -> new RuntimeException("해당 유저가 존재하지 않습니다."));

        Concert concert = concertRepository.findById(concertId)
            .orElseThrow(() -> new RuntimeException("해당 콘서트가 존재하지 않습니다."));

        concert.decreaseTicketAmount();
        concertRepository.save(concert);

        user.setTicketInfo(concert, user.getTicketNumbering() + 1);
        userRepository.save(user);

        return ResponseEntity.ok("티켓 예매 성공");
    }
}
