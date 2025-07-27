package com.salayo.locallifebackend.domain.reservation.dto;

import com.salayo.locallifebackend.domain.payment.dto.PaymentResponseDto;
import com.salayo.locallifebackend.domain.payment.entity.Payment;
import com.salayo.locallifebackend.domain.reservation.entity.Reservation;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ReservationAndPaymentResponseDto {

	private ReservationResponseDto reservationResponseDto;

	private PaymentResponseDto paymentResponseDto;

	public static ReservationAndPaymentResponseDto from(Reservation reservation, Payment payment) {

		return ReservationAndPaymentResponseDto.builder()
			.reservationResponseDto(ReservationResponseDto.from(reservation))
			.paymentResponseDto(PaymentResponseDto.from(payment))
			.build();
	}
}
