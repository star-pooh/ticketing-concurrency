package org.team12.ticketing.concurrency.concert.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.team12.ticketing.concurrency.concert.dto.ConcertRequestDto;
import org.team12.ticketing.concurrency.concert.dto.ConcertResponseDto;
import org.team12.ticketing.concurrency.concert.service.ConcertService;
import org.team12.ticketing.concurrency.user.service.UserService;

@RestController
@RequestMapping("/tickets/concert")
@RequiredArgsConstructor
public class ConcertController {

    private final ConcertService concertService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ConcertResponseDto> createConcert(@RequestBody ConcertRequestDto dto) {
        return ResponseEntity.ok(concertService.createConcert(dto));
    }

    @GetMapping
    public ResponseEntity<ConcertResponseDto> findAllConcert() {
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @PatchMapping("/{concertId}")
    public ResponseEntity<ConcertResponseDto> updateConcert(@PathVariable Long concertId, @RequestBody ConcertRequestDto dto) {
        ConcertResponseDto response = concertService.updateConcert(concertId, dto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{concertId}")
    public ResponseEntity<ConcertResponseDto> buyTicket(@PathVariable Long concertId, @RequestBody ConcertRequestDto dto) {
        ConcertResponseDto response = concertService.buyTicket(concertId, dto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{concertId}/{ticketId}")
    public ResponseEntity<ConcertResponseDto> findTicket(@PathVariable Long concertId, @PathVariable Long ticketId) {
        ConcertResponseDto response = userService.findTicket(concertId, ticketId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
