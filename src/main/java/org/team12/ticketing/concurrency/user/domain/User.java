package org.team12.ticketing.concurrency.user.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.team12.ticketing.concurrency.concert.domain.Concert;

@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nickname;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_id")
    private Concert concert;

    private Long ticketNumbering;

    public User(String nickname) {
        this.nickname = nickname;
    }

    public void TicketInfo(Concert concertId, Long ticketNumbering) {
        this.concert = concertId;
        this.ticketNumbering = ticketNumbering;
    }
}
