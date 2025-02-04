package org.team12.ticketing.concurrency.concert.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.team12.ticketing.concurrency.concert.domain.Concert;
import org.team12.ticketing.concurrency.concert.dto.ConcertRequestDto;
import org.team12.ticketing.concurrency.concert.dto.ConcertResponseDto;
import org.team12.ticketing.concurrency.concert.repository.ConcertRepository;

@Service
@RequiredArgsConstructor
public class ConcertService {

    private final ConcertRepository concertRepository;

    @Transactional
    public ConcertResponseDto updateConcert(Long concertId, ConcertRequestDto dto) {
        Concert foundedConcert = concertRepository.findById(concertId).orElseThrow(() -> new IllegalArgumentException("concert not found"));
        foundedConcert.update(dto);

        return ConcertResponseDto.of(foundedConcert);
    }
}
