package org.team12.ticketing.concurrency.concert.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.team12.ticketing.concurrency.concert.domain.Concert;

@Getter
@RequiredArgsConstructor
public class ConcertResponseDto {

    private final String title;

    private final String singer;

    private final String content;

    private final Long totalTicketAmount;

    private final Long remainTicketAmount;

    public static ConcertResponseDto of(Concert concert) {
        return new ConcertResponseDto(
                concert.getTitle(),
                concert.getSinger(),
                concert.getContent(),
                concert.getTotalTicketAmount(),
                concert.getRemainTicketAmount()
        );
    }
}