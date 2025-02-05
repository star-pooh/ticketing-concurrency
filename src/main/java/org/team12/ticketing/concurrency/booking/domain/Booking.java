package org.team12.ticketing.concurrency.booking.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.team12.ticketing.concurrency.concert.domain.Concert;

@Getter
@NoArgsConstructor
@Entity
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_id")
    private Concert concert;

    @Column(nullable = false)
    private Long bookingOrder;

    @Builder
    public Booking(Concert concert, Long userId) {
        this.concert = concert;
        this.userId = userId;
        this.bookingOrder = concert.getTotalTicketAmount() - concert.getRemainTicketAmount();
    }
}
