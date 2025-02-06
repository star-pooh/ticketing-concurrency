package org.team12.ticketing.concurrency.dbLock;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.team12.ticketing.concurrency.booking.domain.Booking;
import org.team12.ticketing.concurrency.booking.dto.BookingRequestDto;
import org.team12.ticketing.concurrency.booking.dto.BookingResponseDto;
import org.team12.ticketing.concurrency.booking.repository.BookingRepository;
import org.team12.ticketing.concurrency.booking.service.BookingService;
import org.team12.ticketing.concurrency.concert.domain.Concert;
import org.team12.ticketing.concurrency.concert.repository.ConcertRepository;

@ExtendWith(SpringExtension.class)
public class BookingServiceTest {

	private static final Long TEST_CONCERT_ID = 1L;
	private static final Long TOTAL_TICKET_AMOUNT = 100L;
	private static final String TEST_CONCERT_TITLE = "테스트 콘서트";
	private static final Long TEST_USER_ID = 1L;

	@InjectMocks
	private BookingService bookingService;

	@Mock
	private BookingRepository bookingRepository;

	@Mock
	private ConcertRepository concertRepository;

	@Test
	@DisplayName("티켓 예매 시 예매 순서와 남은 티켓 수량이 정상적으로 계산되는지 테스트")
	void bookingOrderCalculationTest() {
		// given
		Concert concert = Concert.builder()
			.title(TEST_CONCERT_TITLE)
			.totalTicketAmount(TOTAL_TICKET_AMOUNT)
			.build();

		given(concertRepository.findById(TEST_CONCERT_ID)).willReturn(Optional.of(concert));
		given(bookingRepository.save(any(Booking.class))).willAnswer(invocation -> invocation.getArgument(0));

		BookingRequestDto bookingRequestDto = new BookingRequestDto(TEST_CONCERT_ID, TEST_USER_ID);

		// when
		BookingResponseDto result = bookingService.bookTicket(1L, bookingRequestDto);

		// then
		assertThat(result.getBookingOrder()).isEqualTo(1L);
		assertThat(concert.getRemainTicketAmount()).isEqualTo(TOTAL_TICKET_AMOUNT - 1);
	}

}
