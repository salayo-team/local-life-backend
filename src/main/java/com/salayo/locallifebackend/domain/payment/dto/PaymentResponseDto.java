package com.salayo.locallifebackend.domain.payment.dto;

import com.salayo.locallifebackend.domain.payment.entity.Payment;
import com.salayo.locallifebackend.domain.payment.enums.PaymentStatus;
import com.salayo.locallifebackend.global.enums.DeletedStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PaymentResponseDto {

	private Long id; //결제 고유 식별자

	private String pgTid; //PG사 거래 고유 ID

	private String impUid; //Iamport Unique ID

	private BigDecimal paymentCost; //결제 금액

	private String paymentCard; //결제 카드 정보

	private String paymentMethodType; //결제 수단 타입

	private PaymentStatus paymentStatus; //결제 상태

	private LocalDateTime refundAttemptedAt; //환불 요청 발생 일시

	private String paymentFailedReason; //PG사 API 응답 메시지

	private LocalDateTime paidAt; //결제 승인일

	private LocalDateTime canceledAt; //결제 취소일

	private LocalDateTime expiredAt; //결제 만료일

	private LocalDateTime createdAt; //결제 생성일

	private LocalDateTime modifiedAt; //결제 수정일

	private DeletedStatus deletedStatus; //결제 삭제 상태

	public static PaymentResponseDto from(Payment payment) {

		return PaymentResponseDto.builder()
			.id(payment.getId())
			.pgTid(payment.getPgTid())
			.impUid(payment.getImpUid())
			.paymentCost(payment.getPaymentCost())
			.paymentCard(payment.getPaymentCard())
			.paymentMethodType(payment.getPaymentMethodType())
			.paymentStatus(payment.getPaymentStatus())
			.refundAttemptedAt(payment.getRefundAttemptedAt())
			.paymentFailedReason(payment.getPaymentFailedReason())
			.paidAt(payment.getPaidAt())
			.canceledAt(payment.getCanceledAt())
			.expiredAt(payment.getExpiredAt())
			.createdAt(payment.getCreatedAt())
			.modifiedAt(payment.getModifiedAt())
			.deletedStatus(payment.getDeletedStatus())
			.build();
	}
}
