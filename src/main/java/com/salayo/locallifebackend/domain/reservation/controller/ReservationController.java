package com.salayo.locallifebackend.domain.reservation.controller;

import com.salayo.locallifebackend.domain.reservation.dto.ReservationCreateRequestDto;
import com.salayo.locallifebackend.domain.reservation.dto.ReservationResponseDto;
import com.salayo.locallifebackend.domain.reservation.service.ReservationService;
import com.salayo.locallifebackend.global.dto.CommonResponseDto;
import com.salayo.locallifebackend.global.security.MemberDetails;
import com.salayo.locallifebackend.global.success.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
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

	private final ReservationService reservationService;

	public ReservationController(ReservationService reservationService) {
		this.reservationService = reservationService;
	}

	/**
	 * 예약 생성 API
	 */
	@Operation(
		summary = "예약 생성",
		description = "멤버가 예약을 생성합니다."
	)
	@PostMapping("/reservations")
	public ResponseEntity<CommonResponseDto<ReservationResponseDto>> createReservation(
		@Valid @RequestBody ReservationCreateRequestDto requestDto, @AuthenticationPrincipal MemberDetails memberDetails) {

		Long memberId = memberDetails.getMember().getId();
		ReservationResponseDto responseDto = reservationService.createReservation(memberId, requestDto);

		return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponseDto.success(SuccessCode.CREATE_SUCCESS, responseDto));
	}

	/**
	 * 예약 테스트용 API
	 */
	@PostMapping("/reservations/test")
	public ResponseEntity<CommonResponseDto<ReservationResponseDto>> testCreateReservation(
		@Valid @RequestBody ReservationCreateRequestDto requestDto) {

		Long memberId = 2L;
		ReservationResponseDto responseDto = reservationService.createReservation(memberId, requestDto);

		return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponseDto.success(SuccessCode.CREATE_SUCCESS, responseDto));
	}

}
