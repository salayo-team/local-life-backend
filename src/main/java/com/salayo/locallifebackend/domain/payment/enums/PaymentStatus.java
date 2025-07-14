package com.salayo.locallifebackend.domain.payment.enums;

public enum PaymentStatus {

	PAYMENT_PENDING, //결제 전 대기
	PAYMENT_SUCCESS, //결제 성공
	PAYMENT_CANCELED, //결제 취소
	PAYMENT_FAILED, //결제 실패
	REFUND_REQUESTED, //환불 요청
	REFUND_FAILED, //환불 실패
	REFUND_COMPLETED, //환불 완료
	;
}
