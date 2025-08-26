package com.salayo.locallifebackend.domain.payment.entity;

import com.salayo.locallifebackend.domain.payment.enums.PaymentStatus;
import com.salayo.locallifebackend.domain.reservation.entity.Reservation;
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

	@Column(nullable = false, length = 100, unique = true)
	private String merchantUid; //주문 고유 번호

	@Column(nullable = false, length = 100)
	private String pgTid; //PG사 거래 고유 ID

	@Column(nullable = true, length = 100)
	private String impUid; //Iamport Unique ID

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal paymentCost; //결제 금액

	@Column(nullable = false, length = 50)
	private String paymentCard; //결제 카드 정보

	@Column(nullable = true)
	private String paymentMethodType; //결제 수단 타입

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private PaymentStatus paymentStatus; //결제 상태

	@Column(nullable = true)
	private LocalDateTime refundAttemptedAt; //환불 요청 발생 일시

	@Column(nullable = true, columnDefinition = "TEXT")
	private String paymentFailedReason; //PG사 API 응답 메시지

	@Column(nullable = true)
	private LocalDateTime paidAt; //결제 승인일

	@Column(nullable = true)
	private LocalDateTime canceledAt; //결제 취소일

	@Column(nullable = true)
	private LocalDateTime expiredAt; //결제 만료일

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private DeletedStatus deletedStatus; //결제 삭제 상태

	@Builder
	public Payment(Reservation reservation, String merchantUid, String pgTid, String impUid, BigDecimal paymentCost, String paymentCard,
		String paymentMethodType, PaymentStatus paymentStatus, LocalDateTime refundAttemptedAt, String paymentFailedReason,
		LocalDateTime paidAt, LocalDateTime canceledAt, LocalDateTime expiredAt, DeletedStatus deletedStatus) {
		this.reservation = reservation;
		this.merchantUid = merchantUid;
		this.pgTid = pgTid;
		this.impUid = impUid;
		this.paymentCost = paymentCost;
		this.paymentCard = paymentCard;
		this.paymentMethodType = paymentMethodType;
		this.paymentStatus = paymentStatus;
		this.refundAttemptedAt = refundAttemptedAt;
		this.paymentFailedReason = paymentFailedReason;
		this.paidAt = paidAt;
		this.canceledAt = canceledAt;
		this.expiredAt = expiredAt;
		this.deletedStatus = deletedStatus;
	}

	/**
	 * 결제 만료 상태 변경 & 시점 기록
	 */
	public void expirePayment(){
		this.paymentStatus = PaymentStatus.PAYMENT_EXPIRED;
		this.expiredAt = LocalDateTime.now();
	}
}
