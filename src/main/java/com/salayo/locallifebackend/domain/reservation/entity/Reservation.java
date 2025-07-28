package com.salayo.locallifebackend.domain.reservation.entity;

import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.programschedule.entity.ProgramSchedule;
import com.salayo.locallifebackend.domain.reservation.enums.ReservationStatus;
import com.salayo.locallifebackend.global.entity.BaseEntity;
import com.salayo.locallifebackend.global.enums.DeletedStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "reservation")
public class Reservation extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; //예약 고유 식별자

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private Member member; //사용자 고유 식별자

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "program_schedule_id", nullable = false)
	private ProgramSchedule programSchedule; //체험 프로그램 스케줄 고유 식별자

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private ReservationStatus reservationStatus; //예약 상태

	@Column(nullable = true, columnDefinition = "TEXT")
	private String rejectedReason; //예약(신청) 거절 사유

	@Column(nullable = true, columnDefinition = "TEXT")
	private String cancelReason; //예약 취소 사유

	@Column(nullable = true)
	private LocalDateTime canceledAt; //예약 취소 일시

	@Column(nullable = true)
	private LocalDateTime rejectedAt; //예약 거절 일시

	@Column(nullable = true)
	private LocalDateTime expiredAt; //예약 만료 일시

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private DeletedStatus deletedStatus; //예약 삭제 상태

	@Builder
	public Reservation(Member member, ProgramSchedule programSchedule, ReservationStatus reservationStatus, String rejectedReason,
		String cancelReason, LocalDateTime canceledAt, LocalDateTime rejectedAt, LocalDateTime expiredAt, DeletedStatus deletedStatus) {
		this.member = member;
		this.programSchedule = programSchedule;
		this.reservationStatus = reservationStatus;
		this.rejectedReason = rejectedReason;
		this.cancelReason = cancelReason;
		this.canceledAt = canceledAt;
		this.rejectedAt = rejectedAt;
		this.expiredAt = expiredAt;
		this.deletedStatus = deletedStatus;
	}

	/**
	 * 예약 상태 변경
	 */
	public void updateReservationStatus(ReservationStatus reservationStatus) {
		this.reservationStatus = reservationStatus;
	}

	/**
	 * 예약 만료 상태 변경 & 시점 기록
	 */
	public void expireReservation(){
		this.reservationStatus = ReservationStatus.EXPIRED;
		this.expiredAt = LocalDateTime.now();
	}
}
