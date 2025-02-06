package org.team12.ticketing.concurrency.booking.service;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.team12.ticketing.concurrency.booking.domain.Booking;
import org.team12.ticketing.concurrency.booking.dto.BookingRequestDto;
import org.team12.ticketing.concurrency.booking.dto.BookingResponseDto;
import org.team12.ticketing.concurrency.booking.repository.BookingRepository;
import org.team12.ticketing.concurrency.concert.domain.Concert;
import org.team12.ticketing.concurrency.concert.repository.ConcertRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {
	private final BookingRepository bookingRepository;
	private final ConcertRepository concertRepository;

	@Transactional
	public BookingResponseDto bookTicket(Long concertId, BookingRequestDto dto) {
		Concert concert = concertRepository.findById(concertId)
			.orElseThrow(() -> new IllegalArgumentException("Concert not found"));

		concert.decreaseRemainTicketAmount(); // ⚠️ 여러 스레드에서 동시 접근하면 Lost Update 위험

		Booking booking = Booking.builder()
			.concert(concert)
			.userId(dto.getUserId())
			.build();

		Booking savedBooking = bookingRepository.save(booking);

		return BookingResponseDto.builder()
			.concertId(savedBooking.getConcert().getId())
			.bookingOrder(savedBooking.getBookingOrder())
			.build();
	}

	public List<BookingResponseDto> findTicket(Long concertId, Long userId) {
		List<Booking> foundBookingList = bookingRepository.findByConcert_IdAndUserId(concertId, userId);

		return foundBookingList.stream()
			.map(booking -> BookingResponseDto.builder()
				.concertId(booking.getConcert().getId())
				.bookingOrder(booking.getBookingOrder())
				.build())
			.toList();
	}

	@Transactional
	public BookingResponseDto bookTicketWithOptimisticLock(BookingRequestDto dto) {
		Concert concert = concertRepository.findByIdWithOptimisticLock(dto.getConcertId())
			.orElseThrow(() -> new IllegalArgumentException("Concert not found"));

		concert.decreaseRemainTicketAmount();

		concertRepository.flush();

		Booking booking = Booking.builder()
			.concert(concert)
			.userId(dto.getUserId())
			.build();

		Booking savedBooking = bookingRepository.save(booking);

		return BookingResponseDto.builder()
			.concertId(savedBooking.getConcert().getId())
			.bookingOrder(savedBooking.getBookingOrder())
			.build();
	}

	@Transactional
	public BookingResponseDto bookTicketWithPessimisticLock(BookingRequestDto dto) {
		Concert concert = concertRepository.findByIdWithPessimisticLock(dto.getConcertId())
			.orElseThrow(() -> new IllegalArgumentException("Concert not found"));

		concert.decreaseRemainTicketAmount();

		Booking booking = Booking.builder()
			.concert(concert)
			.userId(dto.getUserId())
			.build();

		Booking savedBooking = bookingRepository.save(booking);

		return BookingResponseDto.builder()
			.concertId(savedBooking.getConcert().getId())
			.bookingOrder(savedBooking.getBookingOrder())
			.build();
	}

}
