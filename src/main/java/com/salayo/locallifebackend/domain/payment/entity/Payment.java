package com.salayo.locallifebackend.domain.payment.entity;

import com.salayo.locallifebackend.domain.payment.enums.PaymentMethodType;
import com.salayo.locallifebackend.domain.payment.enums.PaymentProvider;
import com.salayo.locallifebackend.domain.payment.enums.PaymentStatus;
import com.salayo.locallifebackend.domain.reservation.entity.Reservation;
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
public class Payment extends SoftDeletableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; //결제 고유 식별자

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reservation_id", nullable = false)
	private Reservation reservation; //예약 고유 식별자

	@Column(nullable = false, length = 100, unique = true)
	private String merchantUid; //주문 고유 번호

	@Column(nullable = true, length = 100)
	private String pgTid; //PG사 거래 고유 ID

	@Column(nullable = true, length = 100)
	private String impUid; //Iamport Unique ID

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal paymentCost; //결제 금액

	@Column(nullable = true, length = 50)
	private String paymentCard; //결제 카드명

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 100)
	private PaymentMethodType paymentMethodType; //결제 수단 타입

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 100)
	private PaymentProvider paymentProvider; //결제 대행사

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private PaymentStatus paymentStatus; //결제 상태

	@Column(nullable = true)
	private LocalDateTime refundedAt; //결제 환불(취소)일

	@Column(nullable = true)
	private LocalDateTime paidAt; //결제 승인일

	@Column(nullable = true)
	private LocalDateTime expiredAt; //결제 만료일

	@Column(nullable = true, length = 100)
	private String paymentCardSnapshot; //카드 스냅샷

	@Column(nullable = true)
	private LocalDateTime lastPaymentFailedAt; //마지막 결제 실패일

	@Column(nullable = true)
	private Integer paymentFailCount; //결제 실패 누적 카운트

	@Column(nullable = true)
	private LocalDateTime lastRefundFailedAt; //마지막 환불 실패일

	@Column(nullable = true)
	private Integer refundFailCount; //환불 실패 누적 카운트

	@Column(nullable = true)
	private BigDecimal totalRefundAmount; //최종 환불 금액

	@Column(nullable = true, columnDefinition = "TEXT")
	private String refundReason; //환불 사유

	@Builder
	public Payment(Reservation reservation, String merchantUid, String pgTid, String impUid, BigDecimal paymentCost, String paymentCard,
		PaymentMethodType paymentMethodType, PaymentProvider paymentProvider, PaymentStatus paymentStatus, LocalDateTime refundedAt,
		LocalDateTime paidAt, LocalDateTime expiredAt, String paymentCardSnapshot,
		LocalDateTime lastPaymentFailedAt,
		Integer paymentFailCount, LocalDateTime lastRefundFailedAt, Integer refundFailCount, BigDecimal totalRefundAmount,
		String refundReason) {
		this.reservation = reservation;
		this.merchantUid = merchantUid;
		this.pgTid = pgTid;
		this.impUid = impUid;
		this.paymentCost = paymentCost;
		this.paymentCard = paymentCard;
		this.paymentMethodType = paymentMethodType;
		this.paymentProvider = paymentProvider;
		this.paymentStatus = paymentStatus;
		this.refundedAt = refundedAt;
		this.paidAt = paidAt;
		this.expiredAt = expiredAt;
		this.paymentCardSnapshot = paymentCardSnapshot;
		this.lastPaymentFailedAt = lastPaymentFailedAt;
		this.paymentFailCount = paymentFailCount;
		this.lastRefundFailedAt = lastRefundFailedAt;
		this.refundFailCount = refundFailCount;
		this.totalRefundAmount = totalRefundAmount;
		this.refundReason = refundReason;
	}

	/**
	 * 결제 만료
	 */
	public void expirePayment() {
		this.paymentStatus = PaymentStatus.PAYMENT_EXPIRED;
		this.expiredAt = LocalDateTime.now();
	}

	/**
	 * 결제 검증 성공시, 결제 데이터 업데이트
	 */
	public void updateAfterPaymentVerification(String pgTid, String impUid, String paymentCard, PaymentMethodType paymentMethodType,
		LocalDateTime paidAt, String paymentCardSnapshot) {
		this.pgTid = pgTid;
		this.impUid = impUid;
		this.paymentCard = paymentCard;
		this.paymentMethodType = paymentMethodType;
		this.paymentStatus = PaymentStatus.PAYMENT_SUCCESS;
		this.paidAt = (paidAt != null) ? paidAt : LocalDateTime.now();
		this.paymentCardSnapshot = paymentCardSnapshot;
	}

	/**
	 * 결제 사전 생성
	 */
	public static Payment preparePayment(Reservation reservation, String merchantUid, String pgTid, String impUid, BigDecimal paymentCost,
		String paymentCard, PaymentProvider paymentProvider, PaymentMethodType paymentMethodType) {

		return Payment.builder()
			.reservation(reservation)
			.merchantUid(merchantUid)
			.pgTid(pgTid)
			.impUid(impUid)
			.paymentCost(paymentCost)
			.paymentCard(paymentCard)
			.paymentProvider(paymentProvider)
			.paymentMethodType(paymentMethodType)
			.paymentStatus(PaymentStatus.PAYMENT_PENDING)
			.build();
	}

	/**
	 * 결제 환불 성공시, 결제 데이터 업데이트
	 */
	public void refundSuccess(LocalDateTime refundedAt, BigDecimal totalRefundAmount, String refundReason) {
		this.refundedAt = (refundedAt != null) ? refundedAt : LocalDateTime.now();
		this.totalRefundAmount = totalRefundAmount;
		this.refundReason = refundReason;
		this.paymentStatus = PaymentStatus.REFUND_COMPLETED;
	}

	/**
	 * 결제 실패 데이터 업데이트
	 */
	public void failPayment() {
		this.lastPaymentFailedAt = LocalDateTime.now();
		this.paymentFailCount = (this.paymentFailCount == null) ? 1 : this.paymentFailCount + 1;
		this.paymentStatus = PaymentStatus.PAYMENT_FAILED;
	}

	/**
	 * 환불 실패 데이터 업데이트
	 */
	public void failRefund() {
		this.lastRefundFailedAt = LocalDateTime.now();
		this.refundFailCount = (this.refundFailCount == null) ? 1 : this.refundFailCount + 1;
		this.paymentStatus = PaymentStatus.REFUND_FAILED;
	}
}
