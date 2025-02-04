package org.team12.ticketing.concurrency.concert.dto;

import lombok.Getter;
import org.team12.ticketing.concurrency.concert.domain.Concert;
import org.team12.ticketing.concurrency.user.domain.User;
import org.team12.ticketing.concurrency.user.dto.UserResponseDto;

@Getter
public class ConcertResponseDto {

    private final String title;

    private final String singer;

    private final String content;

    private final Long ticketAmount;

    private final UserResponseDto user;

    private ConcertResponseDto(String title, String singer, String content, Long ticketAmount, UserResponseDto user) {
        this.title = title;
        this.singer = singer;
        this.content = content;
        this.ticketAmount = ticketAmount;
        this.user = user;
    }

    public static ConcertResponseDto of(Concert concert) {
        return new ConcertResponseDto(
                concert.getTitle(),
                concert.getSinger(),
                concert.getContent(),
                concert.getTicketAmount(),
                null
        );
    }

    public static ConcertResponseDto of(User user) {
        //
        return new ConcertResponseDto(
                user.getConcert().getTitle(),
                user.getConcert().getSinger(),
                user.getConcert().getContent(),
                user.getConcert().getTicketAmount(),
                UserResponseDto.of(user)
        );
    }
}