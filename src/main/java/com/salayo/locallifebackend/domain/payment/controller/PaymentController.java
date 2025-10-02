package com.salayo.locallifebackend.domain.payment.controller;

import com.salayo.locallifebackend.domain.payment.dto.PaymentCreateRequestDto;
import com.salayo.locallifebackend.domain.payment.dto.PaymentResponseDto;
import com.salayo.locallifebackend.domain.payment.service.PaymentService;
import com.salayo.locallifebackend.global.dto.CommonResponseDto;
import com.salayo.locallifebackend.global.security.MemberDetails;
import com.salayo.locallifebackend.global.success.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payment", description = "결제 관련 API")
@RestController
public class PaymentController {

	private final PaymentService paymentService;

	public PaymentController(PaymentService paymentService) {
		this.paymentService = paymentService;
	}


	/**
	 * 결제 생성 API
	 */
	@Operation(
		summary = "결제 생성",
		description = "멤버가 결제를 생성합니다."
	)
	@PostMapping("/payments/{reservationId}")
	public ResponseEntity<CommonResponseDto<PaymentResponseDto>> createPayment(
		@PathVariable("reservationId") Long reservationId,
		@Valid @RequestBody PaymentCreateRequestDto requestDto, @AuthenticationPrincipal MemberDetails memberDetails) {

		Long memberId = memberDetails.getMember().getId();
		PaymentResponseDto paymentResponseDto = paymentService.createPayment(memberId, reservationId, requestDto);

		return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponseDto.success(SuccessCode.CREATE_SUCCESS, paymentResponseDto));
	}

	/**
	 * 결제 생성 테스트용 API
	 */
	@PostMapping("/payments/{reservationId}/test")
	public ResponseEntity<CommonResponseDto<PaymentResponseDto>> testCreatePayment(
		@PathVariable("reservationId") Long reservationId,
		@Valid @RequestBody PaymentCreateRequestDto requestDto) {

		Long memberId = 2L;
		PaymentResponseDto paymentResponseDto = paymentService.createPayment(memberId, reservationId, requestDto);

		return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponseDto.success(SuccessCode.CREATE_SUCCESS, paymentResponseDto));
	}

	/**
	 * 결제 검증 API
	 */
	@PostMapping("/payments/{paymentId}/verify")
	public ResponseEntity<CommonResponseDto<PaymentResponseDto>> verifyPayment(@PathVariable("paymentId") Long paymentId,
		@Valid @RequestBody PaymentCreateRequestDto requestDto, @AuthenticationPrincipal MemberDetails memberDetails) {

		Long memberId = memberDetails.getMember().getId();
		PaymentResponseDto paymentResponseDto = paymentService.verifyPayment(memberId, paymentId, requestDto);

		return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.VERIFICATION_SUCCESS, paymentResponseDto));
	}

	/**
	 * 결제 검증 테스트용 API
	 */
	@PostMapping("/payments/{paymentId}/verify/test")
	public ResponseEntity<CommonResponseDto<PaymentResponseDto>> testVerifyPayment(@PathVariable("paymentId") Long paymentId,
		@Valid @RequestBody PaymentCreateRequestDto requestDto) {

		Long memberId = 2L;
		PaymentResponseDto paymentResponseDto = paymentService.verifyPayment(memberId, paymentId, requestDto);

		return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.VERIFICATION_SUCCESS, paymentResponseDto));
	}


}
