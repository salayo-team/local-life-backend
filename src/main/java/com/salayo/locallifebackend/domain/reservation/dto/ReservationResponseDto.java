package com.salayo.locallifebackend.domain.reservation.dto;

import com.salayo.locallifebackend.domain.reservation.entity.Reservation;
import com.salayo.locallifebackend.domain.reservation.enums.ReservationStatus;
import com.salayo.locallifebackend.global.enums.DeletedStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ReservationResponseDto {

	private final Long id; //예약 고유 식별자

	private final Long userId; //사용자 고유 식별자

	private final Long programScheduleId; //체험 프로그램 스케줄 고유 식별자

	private final ReservationStatus reservationStatus; //예약 상태

	private final LocalDateTime canceledAt; //예약 취소 일시

	private final String cancelReason; //예약 취소 사유

	private final String rejectedReason; //예약(신청) 거절 사유

	private final LocalDateTime rejectedAt; //예약 거절 일시

	private final LocalDateTime expiredAt; //예약 만료 일시

	private final LocalDateTime createdAt; //예약 생성일

	private final LocalDateTime modifiedAt; //예약 수정일

	private final DeletedStatus deletedStatus; //예약 삭제 상태

	public static ReservationResponseDto from(Reservation reservation) {

		return ReservationResponseDto.builder()
			.id(reservation.getId())
			.userId(reservation.getMember().getId())
			.programScheduleId(reservation.getProgramSchedule().getId())
			.reservationStatus(reservation.getReservationStatus())
			.canceledAt(reservation.getCanceledAt())
			.cancelReason(reservation.getCancelReason())
			.rejectedReason(reservation.getRejectedReason())
			.rejectedAt(reservation.getRejectedAt())
			.expiredAt(reservation.getExpiredAt())
			.createdAt(reservation.getCreatedAt())
			.modifiedAt(reservation.getModifiedAt())
			.deletedStatus(reservation.getDeletedStatus())
			.build();
	}
}
