package com.salayo.locallifebackend.domain.payment.entity;

import com.salayo.locallifebackend.domain.payment.enums.PaymentStatus;
import com.salayo.locallifebackend.domain.paymenthistory.entity.PaymentHistory;
import com.salayo.locallifebackend.domain.reservation.entity.Reservation;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "payment")
public class Payment extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; //결제 고유 식별자

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reservation_id", nullable = false)
	private Reservation reservation; //예약 고유 식별자

	@Column(nullable = false, length = 100)
	private String pgTid; //PG사 거래 고유 ID

	@Column(nullable = true, length = 100)
	private String impUid; //Iamport Unique ID

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal paymentCost; //결제 금액

	@Column(nullable = false, length = 50)
	private String paymentCard; //결제 카드 정보

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private PaymentStatus paymentStatus; //결제 상태

	@Column(nullable = true)
	private LocalDateTime refundAttemptedAt; //환불 요청 발생 일시

	@Column(nullable = true, columnDefinition = "TEXT")
	private String refundFailedReason; //PG사 API 응답 메시지

	@Column(nullable = true)
	private LocalDateTime canceledAt; //결제 취소일

	@OneToOne(mappedBy = "payment", fetch = FetchType.LAZY)
	private PaymentHistory paymentHistory;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private DeletedStatus deletedStatus; //결제 삭제 상태

	@Builder

	public Payment(Reservation reservation, String pgTid, String impUid, BigDecimal paymentCost, String paymentCard,
		PaymentStatus paymentStatus, LocalDateTime refundAttemptedAt, String refundFailedReason, LocalDateTime canceledAt,
		PaymentHistory paymentHistory, DeletedStatus deletedStatus) {
		this.reservation = reservation;
		this.pgTid = pgTid;
		this.impUid = impUid;
		this.paymentCost = paymentCost;
		this.paymentCard = paymentCard;
		this.paymentStatus = paymentStatus;
		this.refundAttemptedAt = refundAttemptedAt;
		this.refundFailedReason = refundFailedReason;
		this.canceledAt = canceledAt;
		this.paymentHistory = paymentHistory;
		this.deletedStatus = deletedStatus;
	}
}
