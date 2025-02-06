package org.team12.ticketing.concurrency.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.team12.ticketing.concurrency.booking.dto.BookingRequestDto;

@Service
@RequiredArgsConstructor
public class BookingSynchronizedService {

    private final BookingService bookingService;

    public synchronized void synchronizedBookTicket(Long concertId, BookingRequestDto dto) {
        bookingService.bookTicket(concertId, dto);
    }
}
