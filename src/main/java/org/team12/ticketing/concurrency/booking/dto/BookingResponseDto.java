package org.team12.ticketing.concurrency.booking.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BookingResponseDto {
    private Long concertId;
    private Long bookingOrder;

    @Builder
    public BookingResponseDto(Long concertId, Long bookingOrder) {
        this.concertId = concertId;
        this.bookingOrder = bookingOrder;
    }
}