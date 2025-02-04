package org.team12.ticketing.concurrency.concert.domain;

import io.micrometer.common.util.StringUtils;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.team12.ticketing.concurrency.concert.dto.ConcertRequestDto;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "concert")
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Concert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String singer;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private Long ticketAmount;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public void update(ConcertRequestDto dto) {
        this.title = StringUtils.isBlank(dto.getTitle()) ? this.title : dto.getTitle();
        this.singer = StringUtils.isBlank(dto.getSinger()) ? this.singer : dto.getSinger();
        this.content = StringUtils.isBlank(dto.getContent()) ? this.content : dto.getContent();
        this.ticketAmount = Objects.isNull(dto.getTicketAmount()) ? this.ticketAmount : dto.getTicketAmount();
    }
}
