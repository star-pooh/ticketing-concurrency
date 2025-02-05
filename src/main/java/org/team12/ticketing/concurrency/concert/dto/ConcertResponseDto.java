package org.team12.ticketing.concurrency.concert.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.team12.ticketing.concurrency.concert.domain.Concert;
import org.team12.ticketing.concurrency.user.domain.User;
import org.team12.ticketing.concurrency.user.dto.UserResponseDto;

@Getter
@RequiredArgsConstructor
public class ConcertResponseDto {

	private final String title;

	private final String singer;

	private final String content;

	private final Long ticketAmount;

	private final LocalDateTime createdAt;

	private final UserResponseDto user;

	public static ConcertResponseDto of(Concert concert) {
		return new ConcertResponseDto(
			concert.getTitle(),
			concert.getSinger(),
			concert.getContent(),
			concert.getTicketAmount(),
			concert.getCreatedAt(),
			null
		);
	}

	public static ConcertResponseDto of(User user) {
		return new ConcertResponseDto(
			user.getConcert().getTitle(),
			user.getConcert().getSinger(),
			user.getConcert().getContent(),
			user.getConcert().getTicketAmount(),
			user.getConcert().getCreatedAt(),
			UserResponseDto.of(user)
		);
	}
}