package org.team12.ticketing.concurrency.booking.service;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.team12.ticketing.concurrency.booking.dto.BookingRequestDto;
import org.team12.ticketing.concurrency.booking.dto.BookingResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OptimisticLockBookingService {

	private final BookingService bookingService;

	public BookingResponseDto tryBookingWithRetry(BookingRequestDto bookingRequestDto) {
		int maxRetries = 10;
		int attempt = 0;
		long backoffMillis = 500;

		while (attempt < maxRetries) {
			try {
				return bookingService.bookTicketWithOptimisticLock(bookingRequestDto);
			} catch (ObjectOptimisticLockingFailureException e) {
				attempt++;
				if (attempt >= maxRetries) {
					throw new IllegalStateException("Booking failed after maximum retry attempts. Please try again.");
				}
				try {
					// Exponential backoff (500ms -> 1000ms -> 2000ms)
					Thread.sleep(backoffMillis * (long)Math.pow(2, attempt - 1));
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					throw new IllegalStateException("Booking process was interrupted.");
				}
			}
		}
		throw new IllegalStateException("Booking failed. Please try again.");
	}

}
