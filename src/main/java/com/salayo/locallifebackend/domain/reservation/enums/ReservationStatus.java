package com.salayo.locallifebackend.domain.reservation.enums;

public enum ReservationStatus {

	REQUESTED, //예약 요청
	PAYMENT_PENDING, //결제 대기
	PENDING, //로컬 크리에이터 승인 대기
	APPROVED, //로컬 크리에이터 승인 수락
	REJECTED, //로컬 크리에이터 승인 거절
	CANCELED, //멤버 예약 취소
	COMPLETED, //멤버 체험 완료
	EXPIRED, //예약 만료
	;
}
