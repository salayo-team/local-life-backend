package com.salayo.locallifebackend.domain.reservation.dto;



import com.salayo.locallifebackend.domain.payment.dto.PaymentCreateRequestDto;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ReservationAndPaymentRequestDto {

	@Valid
	private ReservationCreateRequestDto reservationCreateRequestDto;

	@Valid
	private PaymentCreateRequestDto paymentCreateRequestDto;

}
