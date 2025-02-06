package org.team12.ticketing.concurrency.concert.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.team12.ticketing.concurrency.booking.dto.BookingRequestDto;
import org.team12.ticketing.concurrency.booking.dto.BookingResponseDto;
import org.team12.ticketing.concurrency.booking.service.BookingService;
import org.team12.ticketing.concurrency.concert.dto.ConcertCreateRequestDto;
import org.team12.ticketing.concurrency.concert.dto.ConcertResponseDto;
import org.team12.ticketing.concurrency.concert.dto.ConcertUpdateRequestDto;
import org.team12.ticketing.concurrency.concert.service.ConcertService;

import java.util.List;

@RestController
@RequestMapping("/tickets/concert")
@RequiredArgsConstructor
public class ConcertController {

    private final ConcertService concertService;
    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<ConcertResponseDto> createConcert(@RequestBody ConcertCreateRequestDto dto) {
        ConcertResponseDto response = concertService.createConcert(dto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<ConcertResponseDto>> findAllConcerts() {
        List<ConcertResponseDto> response = concertService.findAllConcerts();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/{concertId}")
    public ResponseEntity<ConcertResponseDto> updateConcert(@PathVariable Long concertId, @RequestBody ConcertUpdateRequestDto dto) {
        ConcertResponseDto response = concertService.updateConcert(concertId, dto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{concertId}")
    public ResponseEntity<BookingResponseDto> buyTicket(@PathVariable Long concertId, @RequestBody BookingRequestDto dto) {
        BookingResponseDto response = bookingService.bookingTicket(concertId, dto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{concertId}")
    public ResponseEntity<List<BookingResponseDto>> findTicket(@PathVariable Long concertId, @RequestParam Long userId) {
        List<BookingResponseDto> response = bookingService.findTicket(concertId, userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
