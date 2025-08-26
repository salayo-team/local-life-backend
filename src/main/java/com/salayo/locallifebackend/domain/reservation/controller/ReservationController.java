package com.salayo.locallifebackend.domain.reservation.controller;

import com.salayo.locallifebackend.domain.reservation.dto.ReservationAndPaymentRequestDto;
import com.salayo.locallifebackend.domain.reservation.dto.ReservationAndPaymentResponseDto;
import com.salayo.locallifebackend.domain.reservation.service.ReservationApplicationService;
import com.salayo.locallifebackend.global.dto.CommonResponseDto;
import com.salayo.locallifebackend.global.security.MemberDetails;
import com.salayo.locallifebackend.global.success.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Reservation", description = "예약 관련 API")
@RestController
public class ReservationController {

	private final ReservationApplicationService reservationApplicationService;

	public ReservationController(ReservationApplicationService reservationApplicationService) {
		this.reservationApplicationService = reservationApplicationService;
	}

	/**
	 * 예약 결제 생성 API
	 */
	@Operation(
		summary = "예약 & 결제 생성",
		description = "멤버가 예약과 결제를 생성합니다.",
		security = @SecurityRequirement(name = "bearerAuth")
	)
	@PostMapping("/reservation")
	public ResponseEntity<CommonResponseDto<ReservationAndPaymentResponseDto>> createReservationAndPayment(
		@Valid @RequestBody ReservationAndPaymentRequestDto requestDto,
		@AuthenticationPrincipal MemberDetails memberDetails) {

		Long memberId = memberDetails.getMember().getId();
		ReservationAndPaymentResponseDto responseDto = reservationApplicationService.createReservationAndPayment(memberId, requestDto);

		return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponseDto.success(SuccessCode.CREATE_SUCCESS, responseDto));
	}

}
