package org.team12.ticketing.concurrency.user.dto;

import lombok.Getter;
import org.team12.ticketing.concurrency.user.domain.User;

@Getter
public class UserResponseDto {

    private final Long id;

    private final String nickname;

    private final Long ticketNumbering;

    private UserResponseDto(Long id, String nickname, Long ticketNumbering) {
        this.id = id;
        this.nickname = nickname;
        this.ticketNumbering = ticketNumbering;
    }

    public static UserResponseDto of(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getNickname(),
                user.getTicketNumbering()
        );
    }
}
