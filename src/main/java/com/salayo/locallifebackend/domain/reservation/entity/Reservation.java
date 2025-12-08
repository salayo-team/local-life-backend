package com.salayo.locallifebackend.domain.reservation.entity;

import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.programschedule.entity.ProgramSchedule;
import com.salayo.locallifebackend.domain.reservation.enums.ReservationStatus;
import com.salayo.locallifebackend.global.entity.SoftDeletableEntity;
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
public class Reservation extends SoftDeletableEntity {

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
	private String rejectedReason; //예약 거절 사유

	@Column(nullable = true, columnDefinition = "TEXT")
	private String cancelReason; //예약 취소(환불) 사유

	@Column(nullable = true)
	private LocalDateTime canceledAt; //예약 취소(환불) 일시

	@Column(nullable = true)
	private LocalDateTime rejectedAt; //예약 거절 일시

	@Column(nullable = true)
	private LocalDateTime expiredAt; //예약 만료 일시

	@Builder
	public Reservation(Member member, ProgramSchedule programSchedule, ReservationStatus reservationStatus, String rejectedReason,
		String cancelReason, LocalDateTime canceledAt, LocalDateTime rejectedAt, LocalDateTime expiredAt) {
		this.member = member;
		this.programSchedule = programSchedule;
		this.reservationStatus = reservationStatus;
		this.rejectedReason = rejectedReason;
		this.cancelReason = cancelReason;
		this.canceledAt = canceledAt;
		this.rejectedAt = rejectedAt;
		this.expiredAt = expiredAt;
	}

	/**
	 * 예약 상태 변경
	 * - 예약 요청(REQUESTED) -> 결제 대기(PAYMENT_PENDING)
	 */
	public void changeToPaymentPending() {
		this.reservationStatus = ReservationStatus.PAYMENT_PENDING;
	}

	/**
	 * 예약 상태 변경
	 * - 결제 대기(PAYMENT_PENDING) -> 로컬 크리에이터 승인 대기(AWAITING_APPROVAL)
	 */
	public void changeToAwaitingApproval() {
		this.reservationStatus = ReservationStatus.AWAITING_APPROVAL;
	}

	/**
	 * 예약 만료
	 */
	public void expireReservation() {
		this.reservationStatus = ReservationStatus.EXPIRED;
		this.expiredAt = LocalDateTime.now();
	}

	/**
	 * 예약 취소(환불)
	 */
	public void cancelReservation(String cancelReason) {
		this.reservationStatus = ReservationStatus.CANCELED;
		this.cancelReason = cancelReason;
		this.canceledAt = LocalDateTime.now();
	}

	/**
	 * 예약 상태 변경
	 * - 로컬 크리에이터 승인 완료
	 * - 로컬 크리에이터 승인 대기(AWAITING_APPROVAL) -> 로컬 크리에이터 승인 수락(APPROVED)
	 */
	public void changeToApproved() {
		this.reservationStatus = ReservationStatus.APPROVED;
	}

	/**
	 * 예약 거절
	 * - 로컬 크리에이터 승인 거절
	 * - 로컬 크리에이터 승인 대기(AWAITING_APPROVAL) -> 로컬 크리에이터 승인 거절(REJECTED)
	 */
	public void rejectReservation(String rejectedReason) {
		this.reservationStatus = ReservationStatus.REJECTED;
		this.rejectedReason = rejectedReason;
		this.rejectedAt = LocalDateTime.now();
	}

	/**
	 * 예약 생성
	 */
	public static Reservation createReservation(Member member, ProgramSchedule programSchedule) {

		return Reservation.builder()
			.member(member)
			.programSchedule(programSchedule)
			.reservationStatus(ReservationStatus.REQUESTED)
			.build();
	}

}
