package org.team12.ticketing.concurrency.concert.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.team12.ticketing.concurrency.concert.domain.Concert;

public interface ConcertRepository extends JpaRepository<Concert, Long> {
}
