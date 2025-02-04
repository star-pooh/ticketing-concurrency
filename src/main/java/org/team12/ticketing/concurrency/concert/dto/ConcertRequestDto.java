package org.team12.ticketing.concurrency.concert.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ConcertRequestDto {

	private final String title;

	private final String singer;

	private final String content;

	private final Long ticketAmount;
}
