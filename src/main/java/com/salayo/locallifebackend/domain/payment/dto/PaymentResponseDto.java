package com.salayo.locallifebackend.domain.payment.dto;

import com.salayo.locallifebackend.domain.payment.entity.Payment;
import com.salayo.locallifebackend.domain.payment.enums.PaymentMethodType;
import com.salayo.locallifebackend.domain.payment.enums.PaymentProvider;
import com.salayo.locallifebackend.domain.payment.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PaymentResponseDto {

	private final Long id; //결제 고유 식별자

	private final Long reservationId; //예약 고유 식별자

	private final String merchantUid; //주문 고유 번호

	private final String pgTid; //PG사 거래 고유 ID

	private final String impUid; //Iamport Unique ID

	private final BigDecimal paymentCost; //결제 금액

	private final String paymentCard; //결제 카드명

	private final PaymentMethodType paymentMethodType; //결제 수단 타입

	private final PaymentProvider paymentProvider; //결제 대행사

	private final PaymentStatus paymentStatus; //결제 상태

	private final LocalDateTime refundedAt; //결제 환불(취소)일

	private final LocalDateTime paidAt; //결제 승인일

	private final LocalDateTime expiredAt; //결제 만료일

	private final String paymentCardSnapshot; //카드 스냅샷

	private final LocalDateTime lastPaymentFailedAt; //마지막 결제 실패일

	private final Integer paymentFailCount; //결제 실패 누적 카운트

	private final LocalDateTime lastRefundFailedAt; //마지막 환불 실패일

	private final Integer refundFailCount; //환불 실패 누적 카운트

	private final BigDecimal totalRefundAmount; //최종 환불 금액

	private final String refundReason; //환불 사유

	public static PaymentResponseDto from(Payment payment) {

		return PaymentResponseDto.builder()
			.id(payment.getId())
			.reservationId(payment.getReservation().getId())
			.merchantUid(payment.getMerchantUid())
			.pgTid(payment.getPgTid())
			.impUid(payment.getImpUid())
			.paymentCost(payment.getPaymentCost())
			.paymentCard(payment.getPaymentCard())
			.paymentMethodType(payment.getPaymentMethodType())
			.paymentProvider(payment.getPaymentProvider())
			.paymentStatus(payment.getPaymentStatus())
			.paymentCardSnapshot(payment.getPaymentCardSnapshot())
			.refundedAt(payment.getRefundedAt())
			.paidAt(payment.getPaidAt())
			.expiredAt(payment.getExpiredAt())
			.paymentFailCount(payment.getPaymentFailCount())
			.lastRefundFailedAt(payment.getLastRefundFailedAt())
			.refundFailCount(payment.getRefundFailCount())
			.totalRefundAmount(payment.getTotalRefundAmount())
			.refundReason(payment.getRefundReason())
			.build();
	}
}
