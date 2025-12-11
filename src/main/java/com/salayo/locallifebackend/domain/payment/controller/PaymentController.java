package com.salayo.locallifebackend.domain.payment.controller;

import com.salayo.locallifebackend.domain.payment.dto.PaymentRefundRequestDto;
import com.salayo.locallifebackend.domain.payment.dto.PaymentRequestDto;
import com.salayo.locallifebackend.domain.payment.dto.PaymentResponseDto;
import com.salayo.locallifebackend.domain.payment.service.PaymentService;
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

@Tag(name = "Payment", description = "결제 도메인 API")
@RestController
public class PaymentController {

	private final PaymentService paymentService;

	public PaymentController(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	/**
	 * 결제 사전 생성 API
	 */
	@Operation(
		summary = "결제 사전 생성 API",
		description = """
			- 멤버가 결제를 사전 생성합니다.
			- USER ROLE만 생성 가능합니다.
			- 예약이 REQUESTED 상태인 경우에만 결제 생성이 가능합니다.
			- PortOne 결제 요청 전, 결제 관련 사전 정보를 기록하기 위한 API입니다. 
			"""
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "결제 사전 생성 성공",
			content = @Content(schema = @Schema(implementation = CommonResponseDto.class))),
		@ApiResponse(responseCode = "400", description = "잘못된 요청값 또는 검증 실패"),
		@ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
		@ApiResponse(responseCode = "403", description = "접근 권한 없음"),
		@ApiResponse(responseCode = "404", description = "연관 리소스가 존재하지 않음 (회원, 예약)")
	})
	@PreAuthorize("hasRole('USER')")
	@PostMapping("/payments/{reservationId}")
	public ResponseEntity<CommonResponseDto<PaymentResponseDto>> createPayment(
		@PathVariable("reservationId") Long reservationId,
		@Valid @RequestBody PaymentRequestDto requestDto, @AuthenticationPrincipal MemberDetails memberDetails) {

		Long memberId = memberDetails.getMember().getId();
		PaymentResponseDto paymentResponseDto = paymentService.createPayment(memberId, reservationId, requestDto);

		return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponseDto.success(SuccessCode.CREATE_SUCCESS, paymentResponseDto));
	}

	/**
	 * 결제 검증 API
	 */
	@Operation(
		summary = "결제 검증 API",
		description = """
			- 사전 생성된 결제를 검증합니다.
			- USER ROLE만 검증 가능합니다.
			- PortOne 결제 완료 후, impUid 기반으로 결제 내용을 검증합니다.
			- PortOne 응답 값을 DB 값과 비교해 일치하지 않으면 실패 이력을 생성합니다.
			- 검증 성공 시, 결제 & 예약 상태를 업데이트하고 결제 이력을 생성합니다.
			"""
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "결제 검증 성공",
			content = @Content(schema = @Schema(implementation = CommonResponseDto.class))),
		@ApiResponse(responseCode = "400", description = "잘못된 요청값 또는 검증 실패"),
		@ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
		@ApiResponse(responseCode = "403", description = "접근 권한 없음"),
		@ApiResponse(responseCode = "404", description = "연관 리소스가 존재하지 않음 (회원, 결제, 예약, PortOne)")
	})
	@PreAuthorize("hasRole('USER')")
	@PostMapping("/payments/{paymentId}/verify")
	public ResponseEntity<CommonResponseDto<PaymentResponseDto>> verifyPayment(@PathVariable("paymentId") Long paymentId,
		@Valid @RequestBody PaymentRequestDto requestDto, @AuthenticationPrincipal MemberDetails memberDetails) {

		Long memberId = memberDetails.getMember().getId();
		PaymentResponseDto paymentResponseDto = paymentService.verifyPayment(memberId, paymentId, requestDto);

		return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.VERIFICATION_SUCCESS, paymentResponseDto));
	}

	/**
	 * 관리자 결제 환불 API
	 * - 전액 환불 & 부분 환불
	 */
	@Operation(
		summary = "관리자용 결제 환불 API",
		description = """
			- 관리자 전용 결제 환불 API입니다.
			- ADMIN ROLE만 환불 가능합니다.
			- 결제를 전액 환불하거나, 부분 환불합니다.
			- 예약, 결제 상태에 상관없이 이상 케이스나 운영상 필요에 따라 환불할 수 있습니다.
			- impUid를 사용할 수 없는 경우, merchantUid(주문번호) 기준으로 환불합니다.
			- 환불 실패 시 실패 이력을 생성합니다.
			- 환불 성공 시 결제 상태 및 환불 금액 & 사유를 업데이트하고, 예약을 CANCELED 상태로 변경합니다.
			- 환불 성공 시 결제 이력을 생성합니다.
			"""
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "관리자 결제 환불 성공",
			content = @Content(schema = @Schema(implementation = CommonResponseDto.class))),
		@ApiResponse(responseCode = "400", description = "잘못된 요청값 또는 검증 실패 또는 환불 불가"),
		@ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
		@ApiResponse(responseCode = "403", description = "접근 권한 없음"),
		@ApiResponse(responseCode = "404", description = "연관 리소스가 존재하지 않음 (관리자, 결제, PortOne)")
	})
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/payments/{paymentId}/refund-admin")
	public ResponseEntity<CommonResponseDto<PaymentResponseDto>> refundPaymentForAdmin(@PathVariable("paymentId") Long paymentId,
		@Valid @RequestBody PaymentRefundRequestDto requestDto, @AuthenticationPrincipal MemberDetails memberDetails) {

		Long memberId = memberDetails.getMember().getId();
		PaymentResponseDto paymentResponseDto = paymentService.refundPaymentForAdmin(memberId, paymentId, requestDto);

		return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.REFUND_SUCCESS, paymentResponseDto));
	}

}
