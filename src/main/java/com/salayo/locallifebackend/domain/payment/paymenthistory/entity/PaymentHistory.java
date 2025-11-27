package com.salayo.locallifebackend.domain.payment.paymenthistory.entity;

import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.payment.entity.Payment;
import com.salayo.locallifebackend.domain.payment.enums.PaymentMethodType;
import com.salayo.locallifebackend.domain.payment.enums.PaymentProvider;
import com.salayo.locallifebackend.domain.payment.enums.PaymentStatus;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "payment_history")
public class PaymentHistory extends SoftDeletableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; //결제 내역 고유 식별자

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_id", nullable = false)
	private Payment payment; //결제 고유 식별자

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private Member member; //사용자 고유 식별자

	@Column(nullable = true, length = 100)
	private String merchantUid; //주문 고유 번호

	@Column(nullable = true, length = 100)
	private String pgTid; //PG사 거래 고유 ID

	@Column(nullable = true, length = 100)
	private String impUid; //Iamport Unique ID

	@Column(nullable = true, precision = 12, scale = 2)
	private BigDecimal paymentCost; //결제 금액

	@Enumerated(EnumType.STRING)
	@Column(nullable = true, length = 100)
	private PaymentMethodType paymentMethodType; //결제 수단 타입

	@Enumerated(EnumType.STRING)
	@Column(nullable = true, length = 100)
	private PaymentProvider paymentProvider; //결제 대행사

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private PaymentStatus paymentStatus; //결제 상태

	@Column(nullable = true, columnDefinition = "TEXT")
	private String refundReason; //환불 사유

	@Column(nullable = true)
	private LocalDateTime refundedAt; //결제 환불(취소)일

	@Column(nullable = true)
	private LocalDateTime refundFailedAt; //환불 실패일

	@Column(nullable = true, columnDefinition = "TEXT")
	private String refundFailedReason; //환불 실패 사유 - 내부 기록

	@Column(nullable = true)
	private LocalDateTime paymentFailedAt; //결제 실패일

	@Column(nullable = true, columnDefinition = "TEXT")
	private String paymentFailedReason; //결제 실패 사유 - 내부 기록

	@Column(nullable = true, columnDefinition = "TEXT")
	private String pgFailMessage; //PG사 실패(에러) 메세지

	@Column(nullable = true, length = 100)
	private String paymentCardSnapshot; //카드 스냅샷

	@Column(nullable = true)
	private BigDecimal totalRefundAmount; //최종 환불 금액

	@Builder
	public PaymentHistory(Payment payment, Member member, String merchantUid, String pgTid, String impUid, BigDecimal paymentCost,
		PaymentMethodType paymentMethodType, PaymentProvider paymentProvider, PaymentStatus paymentStatus, String refundReason,
		LocalDateTime refundedAt, LocalDateTime refundFailedAt, String refundFailedReason,
		LocalDateTime paymentFailedAt, String paymentFailedReason, String pgFailMessage, String paymentCardSnapshot,
		BigDecimal totalRefundAmount) {
		this.payment = payment;
		this.member = member;
		this.merchantUid = merchantUid;
		this.pgTid = pgTid;
		this.impUid = impUid;
		this.paymentCost = paymentCost;
		this.paymentMethodType = paymentMethodType;
		this.paymentProvider = paymentProvider;
		this.paymentStatus = paymentStatus;
		this.refundReason = refundReason;
		this.refundedAt = refundedAt;
		this.refundFailedAt = refundFailedAt;
		this.refundFailedReason = refundFailedReason;
		this.paymentFailedAt = paymentFailedAt;
		this.paymentFailedReason = paymentFailedReason;
		this.pgFailMessage = pgFailMessage;
		this.paymentCardSnapshot = paymentCardSnapshot;
		this.totalRefundAmount = totalRefundAmount;
	}

	private static String normalizePgFailMessage(String message) {
		return (message == null || message.isBlank()) ? "PG사 실패 메세지 없음" : message;
	}

	/**
	 * 결제 실패시 결제 실패 내역 생성
	 */
	public static PaymentHistory createPaymentFailurePaymentHistory(Payment payment, Member member, String paymentFailedReason,
		String pgFailMessage) {

		return PaymentHistory.builder()
			.payment(payment)
			.member(member)
			.merchantUid(payment.getMerchantUid())
			.pgTid(payment.getPgTid())
			.impUid(payment.getImpUid())
			.paymentCost(payment.getPaymentCost())
			.paymentMethodType(payment.getPaymentMethodType())
			.paymentProvider(payment.getPaymentProvider())
			.paymentStatus(PaymentStatus.PAYMENT_FAILED)
			.paymentFailedAt(LocalDateTime.now())
			.paymentFailedReason(paymentFailedReason)
			.pgFailMessage(normalizePgFailMessage(pgFailMessage))
			.build();
	}

	/**
	 * 결제 검증 성공시 결제 내역 생성
	 */
	public static PaymentHistory createPaymentSuccessPaymentHistory(Payment payment, Member member, String paymentCardSnapshot) {

		return PaymentHistory.builder()
			.payment(payment)
			.member(member)
			.merchantUid(payment.getMerchantUid())
			.pgTid(payment.getPgTid())
			.impUid(payment.getImpUid())
			.paymentCost(payment.getPaymentCost())
			.paymentMethodType(payment.getPaymentMethodType())
			.paymentProvider(payment.getPaymentProvider())
			.paymentStatus(PaymentStatus.PAYMENT_SUCCESS)
			.paymentCardSnapshot(paymentCardSnapshot)
			.build();
	}

	/**
	 * 환불 실패시 환불 실패 내역 생성
	 */
	public static PaymentHistory createRefundFailurePaymentHistory(Payment payment, Member member,
		String refundFailedReason, String pgFailMessage) {

		return PaymentHistory.builder()
			.payment(payment)
			.member(member)
			.merchantUid(payment.getMerchantUid())
			.pgTid(payment.getPgTid())
			.impUid(payment.getImpUid())
			.paymentCost(payment.getPaymentCost())
			.paymentMethodType(payment.getPaymentMethodType())
			.paymentProvider(payment.getPaymentProvider())
			.paymentStatus(PaymentStatus.REFUND_FAILED)
			.refundFailedAt(LocalDateTime.now())
			.refundFailedReason(refundFailedReason)
			.pgFailMessage(normalizePgFailMessage(pgFailMessage))
			.paymentCardSnapshot(payment.getPaymentCardSnapshot())
			.build();
	}

	/**
	 * 환불 성공시 결제 내역 생성
	 */
	public static PaymentHistory createPaymentRefundSuccessPaymentHistory(Payment payment, Member member) {

		return PaymentHistory.builder()
			.payment(payment)
			.member(member)
			.merchantUid(payment.getMerchantUid())
			.pgTid(payment.getPgTid())
			.impUid(payment.getImpUid())
			.paymentCost(payment.getPaymentCost())
			.paymentMethodType(payment.getPaymentMethodType())
			.paymentProvider(payment.getPaymentProvider())
			.paymentStatus(PaymentStatus.REFUND_COMPLETED)
			.refundReason(payment.getRefundReason())
			.refundedAt(payment.getRefundedAt())
			.paymentCardSnapshot(payment.getPaymentCardSnapshot())
			.totalRefundAmount(payment.getTotalRefundAmount())
			.build();
	}


}
