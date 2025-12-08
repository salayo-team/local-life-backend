package com.salayo.locallifebackend.domain.reservation.enums;

import java.util.EnumSet;
import java.util.Set;

public enum ReservationStatus {

	REQUESTED, //예약 요청
	PAYMENT_PENDING, //결제 대기
	AWAITING_APPROVAL, //로컬 크리에이터 승인 대기
	APPROVED, //로컬 크리에이터 승인 수락
	REJECTED, //로컬 크리에이터 승인 거절
	CANCELED, //예약 취소 & 결제 환불 완료
	COMPLETED, //프로그램 체험 완료
	EXPIRED, //예약 만료
	;

	/**
	 * 예약 비활성 상태
	 * - 예약 생성 가능한 상태
	 */
	public static Set<ReservationStatus> inactiveReservationStatusSet() {
		return EnumSet.of(
			REJECTED,
			CANCELED,
			EXPIRED
		);
	}

	/**
	 * 환불 가능한 예약 상태
	 */
	public static Set<ReservationStatus> refundableReservationStatusSet() {
		return EnumSet.of(
			AWAITING_APPROVAL,
			APPROVED,
			REJECTED
		);
	}

	/**
	 * 멤버가 예약 취소 가능한 상태
	 */
	public static Set<ReservationStatus> memberCancelableStatusSet() {
		return EnumSet.of(
			AWAITING_APPROVAL,
			APPROVED
		);
	}

	/**
	 * 로컬 크리에이터가 예약 거절 가능한 상태
	 */
	public static Set<ReservationStatus> localCreatorRejectableStatusSet() {
		return EnumSet.of(
			AWAITING_APPROVAL
		);
	}

}
