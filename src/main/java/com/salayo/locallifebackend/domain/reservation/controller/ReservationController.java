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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
		summary = "예약 생성 API",
		description = """
			- 멤버가 체험 프로그램 스케줄에 대한 예약을 생성합니다.
			- USER ROLE만 예약 가능합니다.
			- 이미 활성 예약이 존재하는 스케줄에는 중복 예약을 할 수 없습니다.
			"""
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "예약 생성 성공",
			content = @Content(schema = @Schema(implementation = CommonResponseDto.class))),
		@ApiResponse(responseCode = "400", description = "잘못된 요청값 또는 검증 실패"),
		@ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
		@ApiResponse(responseCode = "403", description = "접근 권한 없음"),
		@ApiResponse(responseCode = "404", description = "연관 리소스가 존재하지 않음 (회원, 예약 대상 프로그램 스케줄)")
	})
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
	 */
	@Operation(
		summary = "예약 취소(환불)",
		description = """
			- 멤버가 예약을 취소하고 결제 환불까지 함께 처리됩니다.
			- 예약 소유 및 상태 검증 후 결제 조회 및 환불 처리를 합니다.
			- 환불이 완료되면 예약을 CANCELED 상태로 변경 후 취소일과 취소 사유를 업데이트 합니다.
			- USER ROLE만 취소 가능합니다.
			"""
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "예약 취소 및 환불 성공",
			content = @Content(schema = @Schema(implementation = CommonResponseDto.class))),
		@ApiResponse(responseCode = "400", description = "잘못된 요청값 또는 검증 실패 또는 환불 불가"),
		@ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
		@ApiResponse(responseCode = "403", description = "접근 권한 없음"),
		@ApiResponse(responseCode = "404", description = "연관 리소스가 존재하지 않음 (회원, 예약, 결제)")
	})
	@PreAuthorize("hasRole('USER')")
	@PostMapping("/reservations/{reservationId}/cancel")
	public ResponseEntity<CommonResponseDto<ReservationResponseDto>> reservationCancel(@PathVariable("reservationId") Long reservationId,
		@Valid @RequestBody ReservationCancelRequestDto requestDto, @AuthenticationPrincipal MemberDetails memberDetails) {

		Long memberId = memberDetails.getMember().getId();
		ReservationResponseDto responseDto = reservationAndPaymentApplicationService.cancelReservationWithRefund(memberId, reservationId,
			requestDto);

		return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.RESERVATION_CANCEL_AND_REFUND_SUCCESS, responseDto));
	}

	/**
	 * 예약 거절 API
	 * - 거절시 결제 환불 진행
	 */
	@Operation(
		summary = "예약 거절(환불)",
		description = """
			- 로컬 크리에이터가 멤버가 신청한 예약을 거절하고 결제 환불까지 함께 처리됩니다.
			- 프로그램 소유 및 상태 검증 후 결제 조회 및 환불 처리를 합니다.
			- 환불이 완료되면 예약을 REJECTED 상태로 변경 후 거절일과 거절 사유를 업데이트 합니다.
			- LOCAL CREATOR ROLE만 거절 가능합니다.
			"""
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "예약 거절 및 환불 성공",
			content = @Content(schema = @Schema(implementation = CommonResponseDto.class))),
		@ApiResponse(responseCode = "400", description = "잘못된 요청값 또는 검증 실패 또는 환불 불가"),
		@ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
		@ApiResponse(responseCode = "403", description = "접근 권한 없음"),
		@ApiResponse(responseCode = "404", description = "연관 리소스가 존재하지 않음 (회원, 예약, 결제)")
	})
	@PreAuthorize("hasRole('LOCAL_CREATOR')")
	@PostMapping("/reservations/{reservationId}/reject")
	public ResponseEntity<CommonResponseDto<ReservationResponseDto>> reservationReject(@PathVariable("reservationId") Long reservationId,
		@Valid @RequestBody ReservationRejectRequestDto requestDto, @AuthenticationPrincipal MemberDetails memberDetails) {

		Long memberId = memberDetails.getMember().getId();
		ReservationResponseDto responseDto = reservationAndPaymentApplicationService.rejectReservationWithRefund(memberId, reservationId,
			requestDto);

		return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.RESERVATION_REJECT_AND_REFUND_SUCCESS, responseDto));
	}


}
