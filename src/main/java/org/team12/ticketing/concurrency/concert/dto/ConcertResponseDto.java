package org.team12.ticketing.concurrency.concert.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import org.team12.ticketing.concurrency.concert.domain.Concert;

@Getter
public class ConcertResponseDto {

    private final String title;

    private final String singer;

    private final String content;

    private final Long ticketAmount;

    private final LocalDateTime createdAt;

    private ConcertResponseDto(String title, String singer, String content, Long ticketAmount, LocalDateTime createdAt) {
        this.title = title;
        this.singer = singer;
        this.content = content;
        this.ticketAmount = ticketAmount;
        this.createdAt = createdAt;
    }

    public static ConcertResponseDto of(Concert concert) {
        return new ConcertResponseDto(
                concert.getTitle(),
                concert.getSinger(),
                concert.getContent(),
                concert.getTicketAmount(),
                concert.getCreatedAt()
        );
    }
}