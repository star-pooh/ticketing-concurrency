package org.team12.ticketing.concurrency.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.team12.ticketing.concurrency.concert.dto.ConcertResponseDto;
import org.team12.ticketing.concurrency.user.domain.User;
import org.team12.ticketing.concurrency.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public ConcertResponseDto findTicket(Long concertId, Long ticketId) {
        User foundUser = userRepository.findByTicketNumbering(concertId, ticketId);
        return ConcertResponseDto.of(foundUser);
    }
}
