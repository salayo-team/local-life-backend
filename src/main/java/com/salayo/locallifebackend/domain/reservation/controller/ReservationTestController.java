package com.salayo.locallifebackend.domain.reservation.controller;

import com.salayo.locallifebackend.domain.reservation.dto.ReservationCancelRequestDto;
import com.salayo.locallifebackend.domain.reservation.dto.ReservationCreateRequestDto;
import com.salayo.locallifebackend.domain.reservation.dto.ReservationRejectRequestDto;
import com.salayo.locallifebackend.domain.reservation.dto.ReservationResponseDto;
import com.salayo.locallifebackend.domain.reservation.service.ReservationAndPaymentApplicationService;
import com.salayo.locallifebackend.domain.reservation.service.ReservationService;
import com.salayo.locallifebackend.global.dto.CommonResponseDto;
import com.salayo.locallifebackend.global.security.MemberDetails;
import com.salayo.locallifebackend.global.success.SuccessCode;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Reservation-Test", description = "예약 테스트 API")
@RestController
public class ReservationTestController {

	private final ReservationService reservationService;
	private final ReservationAndPaymentApplicationService reservationAndPaymentApplicationService;

	public ReservationTestController(ReservationService reservationService,
		ReservationAndPaymentApplicationService reservationAndPaymentApplicationService) {
		this.reservationService = reservationService;
		this.reservationAndPaymentApplicationService = reservationAndPaymentApplicationService;
	}

	/**
	 * 예약 테스트 API
	 */
	@PostMapping("/reservations/test")
	public ResponseEntity<CommonResponseDto<ReservationResponseDto>> testCreateReservation(
		@Valid @RequestBody ReservationCreateRequestDto requestDto) {

		Long memberId = 2L;
		ReservationResponseDto responseDto = reservationService.createReservation(memberId, requestDto);

		return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponseDto.success(SuccessCode.CREATE_SUCCESS, responseDto));
	}

	/**
	 * 예약 취소(환불) 테스트 API
	 * - 사용자(멤버)가 예약 취소
	 * - 취소시 결제 환불 진행
	 */
	@PostMapping("/reservations/{reservationId}/cancel/test")
	public ResponseEntity<CommonResponseDto<ReservationResponseDto>> reservationCancel(@PathVariable("reservationId") Long reservationId,
		@Valid @RequestBody ReservationCancelRequestDto requestDto, @AuthenticationPrincipal MemberDetails memberDetails) {

		Long memberId = 2L;
		ReservationResponseDto responseDto = reservationAndPaymentApplicationService.cancelReservationWithRefund(memberId, reservationId,
			requestDto);

		return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponseDto.success(SuccessCode.REFUND_SUCCESS, responseDto));
	}

	/**
	 * 예약 거절 테스트 API
	 * - 로컬 크리에이터가 예약 거절
	 * - 거절시 결제 환불 진행
	 */
	@PostMapping("/reservations/{reservationId}/reject/test")
	public ResponseEntity<CommonResponseDto<ReservationResponseDto>> reservationReject(@PathVariable("reservationId") Long reservationId,
		@Valid @RequestBody ReservationRejectRequestDto requestDto, @AuthenticationPrincipal MemberDetails memberDetails) {

		Long memberId = 1L;
		ReservationResponseDto responseDto = reservationAndPaymentApplicationService.rejectReservationWithRefund(memberId, reservationId,
			requestDto);

		return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponseDto.success(SuccessCode.REFUND_SUCCESS, responseDto));
	}

}
