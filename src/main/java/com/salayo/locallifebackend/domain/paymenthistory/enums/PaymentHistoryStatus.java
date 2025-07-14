package com.salayo.locallifebackend.domain.paymenthistory.enums;

public enum PaymentHistoryStatus {

	PAYMENT_CANCELED, //결제 취소
	PAYMENT_SUCCESS, //결제 성공
	REFUND_REQUESTED, //환불 트리거 - PG사 환불 요청
	REFUND_FAILED, //PG사 API 실패로 환불 실패
	REFUND_COMPLETED, //환불 성공
	;
}
