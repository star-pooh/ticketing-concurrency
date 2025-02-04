package org.team12.ticketing.concurrency.concert.service;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

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
    public ConcertResponseDto createConcert(ConcertRequestDto dto) {
        Concert concert = new Concert(dto.getTitle(), dto.getSinger(), dto.getContent(), dto.getTicketAmount());
        Concert savedConcert = concertRepository.save(concert);
        return ConcertResponseDto.of(savedConcert);
    }

    @Transactional
    public ConcertResponseDto updateConcert(Long concertId, ConcertRequestDto dto) {
        Concert foundedConcert = concertRepository.findById(concertId).orElseThrow(() -> new IllegalArgumentException("concert not found"));
        foundedConcert.update(dto);

        return ConcertResponseDto.of(foundedConcert);
    }

    @Transactional
    public ConcertResponseDto buyTicket(Long concertId, ConcertRequestDto requestDto) {
        User user = userRepository.findById(requestDto.getUserId())
            .orElseThrow(() -> new RuntimeException("해당 유저가 존재하지 않습니다."));

        Concert concert = concertRepository.findById(concertId)
            .orElseThrow(() -> new RuntimeException("해당 콘서트가 존재하지 않습니다."));

        //티켓 번호 저장 - 현재 남은 티켓 갯수 그대로 사용 (동시성 문제 발생)
        Long ticketNumber = concert.getTicketAmount();

        //현재 남은 티켓 갯수 - 1
        concert.decreaseTicketAmount();
        concertRepository.save(concert);

        //저장된 티켓 번호 유저에게 할당
        user.setTicketInfo(concert, ticketNumber);
        userRepository.save(user);

        return ConcertResponseDto.of(concert);
    }

    public List<ConcertResponseDto> findAllConcerts() {
        return concertRepository.findAll().stream()
            .map(ConcertResponseDto::of)
            .collect(Collectors.toList());
    }
}
