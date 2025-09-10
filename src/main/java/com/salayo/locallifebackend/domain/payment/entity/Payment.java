package com.salayo.locallifebackend.domain.payment.entity;

import com.salayo.locallifebackend.domain.payment.enums.PaymentMethodType;
import com.salayo.locallifebackend.domain.payment.enums.PaymentProvider;
import com.salayo.locallifebackend.domain.payment.enums.PaymentStatus;
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

	@Column(nullable = false, length = 100, unique = true)
	private String merchantUid; //주문 고유 번호

	@Column(nullable = false, length = 100)
	private String pgTid; //PG사 거래 고유 ID

	@Column(nullable = true, length = 100)
	private String impUid; //Iamport Unique ID

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal paymentCost; //결제 금액

	@Column(nullable = false, length = 50)
	private String paymentCard; //결제 카드명

	@Enumerated(EnumType.STRING)
	@Column(nullable = true, length = 100)
	private PaymentMethodType paymentMethodType; //결제 수단 타입

	@Enumerated(EnumType.STRING)
	@Column(nullable = true, length = 100)
	private PaymentProvider paymentProvider; //결제 대행사

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private PaymentStatus paymentStatus; //결제 상태

	@Column(nullable = true)
	private LocalDateTime refundAttemptedAt; //환불 요청 발생 일시

	@Column(nullable = true, columnDefinition = "TEXT")
	private String paymentFailedReason; //실패 사유 - 내부 기록

	@Column(nullable = true)
	private LocalDateTime paidAt; //결제 승인일

	@Column(nullable = true)
	private LocalDateTime canceledAt; //결제 취소일

	@Column(nullable = true)
	private LocalDateTime expiredAt; //결제 만료일

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private DeletedStatus deletedStatus; //결제 삭제 상태

	@Column(nullable = true)
	private LocalDateTime paymentFailedAt; //결제 실패일

	@Column(nullable = true, columnDefinition = "TEXT")
	private String pgFailMessage; //PG사 측 실패 메세지

	@Column(nullable = true, length = 100)
	private String paymentCardSnapshot; //카드 스냅샷

	@Builder
	public Payment(Reservation reservation, String merchantUid, String pgTid, String impUid, BigDecimal paymentCost, String paymentCard,
		PaymentMethodType paymentMethodType, PaymentProvider paymentProvider, PaymentStatus paymentStatus, LocalDateTime refundAttemptedAt, String paymentFailedReason,
		LocalDateTime paidAt, LocalDateTime canceledAt, LocalDateTime expiredAt, DeletedStatus deletedStatus, LocalDateTime paymentFailedAt,
		String pgFailMessage, String paymentCardSnapshot) {
		this.reservation = reservation;
		this.merchantUid = merchantUid;
		this.pgTid = pgTid;
		this.impUid = impUid;
		this.paymentCost = paymentCost;
		this.paymentCard = paymentCard;
		this.paymentMethodType = paymentMethodType;
		this.paymentProvider = paymentProvider;
		this.paymentStatus = paymentStatus;
		this.refundAttemptedAt = refundAttemptedAt;
		this.paymentFailedReason = paymentFailedReason;
		this.paidAt = paidAt;
		this.canceledAt = canceledAt;
		this.expiredAt = expiredAt;
		this.deletedStatus = deletedStatus;
		this.paymentFailedAt = paymentFailedAt;
		this.pgFailMessage = pgFailMessage;
		this.paymentCardSnapshot = paymentCardSnapshot;
	}

	/**
	 * 결제 만료 상태 변경 & 시점 기록
	 */
	public void expirePayment() {
		this.paymentStatus = PaymentStatus.PAYMENT_EXPIRED;
		this.expiredAt = LocalDateTime.now();
	}

	/**
	 * 결제 검증 성공시, 상태 변경 및 결제 승인일 저장
	 */
	public void verifySuccess(LocalDateTime paidAt) {
		this.paymentStatus = PaymentStatus.PAYMENT_SUCCESS;
		this.paidAt = paidAt;
	}

	/**
	 * 결제 데이터 업데이트
	 */
	public void updatePayment(String pgTid, String impUid, String paymentCard, PaymentMethodType paymentMethodType) {
		this.pgTid = pgTid;
		this.impUid = impUid;
		this.paymentCard = paymentCard;
		this.paymentMethodType = paymentMethodType;
	}

	/**
	 * 결제 실패 기록
	 */
	public void failPayment(String paymentFailedReason, String pgFailMessage) {
		this.paymentStatus = PaymentStatus.PAYMENT_FAILED;
		this.paymentFailedReason = paymentFailedReason;
		this.pgFailMessage = (pgFailMessage != null && !pgFailMessage.isBlank()) ? pgFailMessage : "PG사 실패 메세지 없음";
		this.paymentFailedAt = LocalDateTime.now();
	}

	/**
	 * 결제 카드 스냅샷 저장
	 */
	public void updateCardSnapshot(String paymentCardSnapshot){
		this.paymentCardSnapshot = paymentCardSnapshot;
	}
}
