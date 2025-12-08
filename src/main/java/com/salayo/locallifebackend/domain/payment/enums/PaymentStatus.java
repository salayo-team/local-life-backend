package com.salayo.locallifebackend.domain.payment.enums;

import java.util.EnumSet;
import java.util.Set;

public enum PaymentStatus {

	PAYMENT_PENDING, //결제 전 대기
	PAYMENT_SUCCESS, //결제 성공
	PAYMENT_CANCELED, //결제 취소 - TODO : 필요 없으면 삭제 예정
	PAYMENT_FAILED, //결제 실패
	REFUND_REQUESTED, //환불 요청 - TODO : 필요 없으면 삭제 예정
	REFUND_FAILED, //환불 실패
	REFUND_COMPLETED, //환불 완료
	PAYMENT_EXPIRED, //결제 만료
	;

	/**
	 * 결제 환불 활성화 상태
	 */
	public static Set<PaymentStatus> refundablePaymentStatusSet() {
		return EnumSet.of(
			PAYMENT_SUCCESS,
			REFUND_FAILED
		);
	}
}
