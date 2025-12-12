package com.salayo.locallifebackend.domain.payment.controller;

import com.salayo.locallifebackend.domain.payment.dto.PaymentRefundRequestDto;
import com.salayo.locallifebackend.domain.payment.dto.PaymentRequestDto;
import com.salayo.locallifebackend.domain.payment.dto.PaymentResponseDto;
import com.salayo.locallifebackend.domain.payment.service.PaymentService;
import com.salayo.locallifebackend.global.dto.CommonResponseDto;
import com.salayo.locallifebackend.global.success.SuccessCode;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payment-Test",
	description = """
		- 결제 테스트 API
		- 로컬 테스트 이후 삭제 예정입니다.
		"""
)
@RestController
public class PaymentTestController {

	private final PaymentService paymentService;

	public PaymentTestController(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	/**
	 * 결제 생성 테스트용 API
	 */
	@PostMapping("/payments/{reservationId}/test")
	public ResponseEntity<CommonResponseDto<PaymentResponseDto>> testCreatePayment(
		@PathVariable("reservationId") Long reservationId,
		@Valid @RequestBody PaymentRequestDto requestDto) {

		Long memberId = 2L;
		PaymentResponseDto paymentResponseDto = paymentService.createPayment(memberId, reservationId, requestDto);

		return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponseDto.success(SuccessCode.CREATE_SUCCESS, paymentResponseDto));
	}

	/**
	 * 결제 검증 테스트용 API
	 */
	@PostMapping("/payments/{paymentId}/verify/test")
	public ResponseEntity<CommonResponseDto<PaymentResponseDto>> testVerifyPayment(@PathVariable("paymentId") Long paymentId,
		@Valid @RequestBody PaymentRequestDto requestDto) {

		Long memberId = 2L;
		PaymentResponseDto paymentResponseDto = paymentService.verifyPayment(memberId, paymentId, requestDto);

		return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.VERIFICATION_SUCCESS, paymentResponseDto));
	}

	/**
	 * 관리자 결제 환불 테스트 API
	 * - 전액 환불 & 부분 환불
	 */
	@PostMapping("/payments/{paymentId}/refund-admin/test")
	public ResponseEntity<CommonResponseDto<PaymentResponseDto>> refundPaymentForAdmin(@PathVariable("paymentId") Long paymentId,
		@Valid @RequestBody PaymentRefundRequestDto requestDto) {

		Long memberId = 3L;
		PaymentResponseDto paymentResponseDto = paymentService.refundPaymentForAdmin(memberId, paymentId, requestDto);

		return ResponseEntity.ok(CommonResponseDto.success(SuccessCode.REFUND_SUCCESS, paymentResponseDto));
	}

}
