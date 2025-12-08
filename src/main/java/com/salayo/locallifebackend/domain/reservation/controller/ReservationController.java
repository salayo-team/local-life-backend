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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Reservation", description = "예약 도메인 API")
@RestController
public class ReservationController {

	private final ReservationService reservationService;
	private final ReservationAndPaymentApplicationService reservationAndPaymentApplicationService;

	public ReservationController(ReservationService reservationService,
		ReservationAndPaymentApplicationService reservationAndPaymentApplicationService) {
		this.reservationService = reservationService;
		this.reservationAndPaymentApplicationService = reservationAndPaymentApplicationService;
	}

	/**
	 * 예약 생성 API
	 */
	@Operation(
		summary = "예약 생성",
		description = "멤버가 예약을 생성합니다."
	)
	@PreAuthorize("hasRole('USER')")
	@PostMapping("/reservations")
	public ResponseEntity<CommonResponseDto<ReservationResponseDto>> createReservation(
		@Valid @RequestBody ReservationCreateRequestDto requestDto, @AuthenticationPrincipal MemberDetails memberDetails) {

		Long memberId = memberDetails.getMember().getId();
		ReservationResponseDto responseDto = reservationService.createReservation(memberId, requestDto);

		return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponseDto.success(SuccessCode.CREATE_SUCCESS, responseDto));
	}

	/**
	 * 예약 취소(환불) API
	 * - 사용자(멤버)가 예약 취소
	 * - 취소시 결제 환불 진행
	 */
	@Operation(
		summary = "예약 취소(환불) ",
		description = "멤버가 예약을 취소(환불)합니다."
	)
	@PreAuthorize("hasRole('USER')")
	@PostMapping("/reservations/{reservationId}/cancel")
	public ResponseEntity<CommonResponseDto<ReservationResponseDto>> reservationCancel(@PathVariable("reservationId") Long reservationId,
		@Valid @RequestBody ReservationCancelRequestDto requestDto, @AuthenticationPrincipal MemberDetails memberDetails) {

		Long memberId = memberDetails.getMember().getId();
		ReservationResponseDto responseDto = reservationAndPaymentApplicationService.cancelReservationWithRefund(memberId, reservationId,
			requestDto);

		return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponseDto.success(SuccessCode.REFUND_SUCCESS, responseDto));
	}

	/**
	 * 예약 거절 API
	 * - 로컬 크리에이터가 예약 거절
	 * - 거절시 결제 환불 진행
	 */
	@Operation(
		summary = "예약 거절(환불) ",
		description = "로컬 크리에이터가 멤버가 신청한 예약을 거절(환불)합니다."
	)
	@PreAuthorize("hasRole('LOCAL_CREATOR')")
	@PostMapping("/reservations/{reservationId}/reject")
	public ResponseEntity<CommonResponseDto<ReservationResponseDto>> reservationReject(@PathVariable("reservationId") Long reservationId,
		@Valid @RequestBody ReservationRejectRequestDto requestDto, @AuthenticationPrincipal MemberDetails memberDetails) {

		Long memberId = memberDetails.getMember().getId();
		ReservationResponseDto responseDto = reservationAndPaymentApplicationService.rejectReservationWithRefund(memberId, reservationId,
			requestDto);

		return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponseDto.success(SuccessCode.REFUND_SUCCESS, responseDto));
	}


}
