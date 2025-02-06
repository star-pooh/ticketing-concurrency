package org.team12.ticketing.concurrency.concert.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.team12.ticketing.concurrency.concert.domain.Concert;

import jakarta.persistence.LockModeType;

public interface ConcertRepository extends JpaRepository<Concert, Long> {

	@Lock(LockModeType.OPTIMISTIC)
	@Query("select c from Concert c where c.id = :id")
	Optional<Concert> findByIdWithOptimisticLock(@Param("id") Long id);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select c from Concert c where c.id = :id")
	Optional<Concert> findByIdWithPessimisticLock(@Param("id") Long id);
}
