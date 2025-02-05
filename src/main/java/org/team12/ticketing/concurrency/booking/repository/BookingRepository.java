package org.team12.ticketing.concurrency.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.team12.ticketing.concurrency.booking.domain.Booking;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByConcertId(Long concertId);

    @Query("SELECT b FROM Booking b JOIN FETCH b.concert WHERE b.concert.id = :concertId AND b.userId = :userId")
    List<Booking> findByConcert_IdAndUserId(Long concertId, Long userId);
}