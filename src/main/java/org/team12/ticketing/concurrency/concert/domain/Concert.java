package org.team12.ticketing.concurrency.concert.domain;

import io.micrometer.common.util.StringUtils;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.team12.ticketing.concurrency.concert.dto.ConcertUpdateRequestDto;

import java.time.LocalDateTime;

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
    private Long totalTicketAmount;

    @Column(nullable = false)
    private Long remainTicketAmount;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Version
    private Long version;

    @Builder
    public Concert(String title, String singer, String content, Long totalTicketAmount) {
        this.title = title;
        this.singer = singer;
        this.content = content;
        this.totalTicketAmount = totalTicketAmount;
        this.remainTicketAmount = totalTicketAmount;
    }

    public void update(ConcertUpdateRequestDto dto) {
        this.title = StringUtils.isBlank(dto.getTitle()) ? this.title : dto.getTitle();
        this.singer = StringUtils.isBlank(dto.getSinger()) ? this.singer : dto.getSinger();
        this.content = StringUtils.isBlank(dto.getContent()) ? this.content : dto.getContent();
    }

    public void decreaseRemainTicketAmount() {
        if (this.remainTicketAmount <= 0) {
            throw new RuntimeException("티켓이 매진 되었습니다.");
        }

        this.remainTicketAmount--;
    }
}
