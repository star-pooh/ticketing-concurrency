package org.team12.ticketing.concurrency;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class TicketingConcurrencyApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketingConcurrencyApplication.class, args);
    }

}
