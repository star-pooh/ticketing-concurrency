package org.team12.ticketing.concurrency.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.team12.ticketing.concurrency.user.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u JOIN FETCH u.concert WHERE u.concert.id = :concertId AND u.ticketNumbering = :ticketId")
    User findByTicketNumbering(Long concertId, Long ticketId);
}
