package org.team12.ticketing.concurrency.concert.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.team12.ticketing.concurrency.concert.domain.Concert;
import org.team12.ticketing.concurrency.concert.dto.ConcertCreateRequestDto;
import org.team12.ticketing.concurrency.concert.dto.ConcertResponseDto;
import org.team12.ticketing.concurrency.concert.dto.ConcertUpdateRequestDto;
import org.team12.ticketing.concurrency.concert.repository.ConcertRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConcertService {

    private final ConcertRepository concertRepository;

    @Transactional
    public ConcertResponseDto createConcert(ConcertCreateRequestDto dto) {
        Concert concert = new Concert(dto.getTitle(), dto.getSinger(), dto.getContent(), dto.getTotalTicketAmount());
        Concert savedConcert = concertRepository.save(concert);
        return ConcertResponseDto.of(savedConcert);
    }

    @Transactional
    public ConcertResponseDto updateConcert(Long concertId, ConcertUpdateRequestDto dto) {
        Concert foundedConcert = concertRepository.findById(concertId).orElseThrow(() -> new IllegalArgumentException("concert not found"));
        foundedConcert.update(dto);

        return ConcertResponseDto.of(foundedConcert);
    }

    public List<ConcertResponseDto> findAllConcerts() {
        return concertRepository.findAll().stream()
                .map(ConcertResponseDto::of)
                .collect(Collectors.toList());
    }
}
