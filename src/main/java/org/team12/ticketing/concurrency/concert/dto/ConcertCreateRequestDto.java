package org.team12.ticketing.concurrency.concert.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ConcertCreateRequestDto {

    private final String title;

    private final String singer;

    private final String content;

    private final Long totalTicketAmount;

    private final Long remainTicketAmount;
}
