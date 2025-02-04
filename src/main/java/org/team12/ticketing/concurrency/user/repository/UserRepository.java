package org.team12.ticketing.concurrency.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.team12.ticketing.concurrency.user.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
