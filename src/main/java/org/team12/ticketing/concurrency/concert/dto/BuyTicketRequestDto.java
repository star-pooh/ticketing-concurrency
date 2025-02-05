package org.team12.ticketing.concurrency.concert.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BuyTicketRequestDto {

	private final Long userId;
}
