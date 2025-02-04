package org.team12.ticketing.concurrency.concert.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.team12.ticketing.concurrency.concert.dto.ConcertRequestDto;
import org.team12.ticketing.concurrency.concert.dto.ConcertResponseDto;
import org.team12.ticketing.concurrency.concert.service.ConcertService;

@RestController
@RequestMapping("/tickets/concert")
@RequiredArgsConstructor
public class ConcertController {

    private final ConcertService concertService;

    @GetMapping
    public ResponseEntity<ConcertResponseDto> findAllConcert() {
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @PatchMapping("/{concertId}")
    public ResponseEntity<ConcertResponseDto> updateConcert(@PathVariable Long concertId, @RequestBody ConcertRequestDto dto) {
        ConcertResponseDto concertResponseDto = concertService.updateConcert(concertId, dto);
        return new ResponseEntity<>(concertResponseDto, HttpStatus.OK);
    }

    @PostMapping("/{concertId}")
    public ResponseEntity<ConcertResponseDto> buyTicket(@PathVariable Long concertId, @RequestBody ConcertRequestDto dto) {
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @GetMapping("/{concertId}/{ticketId}")
    public ResponseEntity<ConcertResponseDto> findTicket(@PathVariable Long concertId, @PathVariable Long ticketId) {
        return new ResponseEntity<>(null, HttpStatus.OK);
    }
}
