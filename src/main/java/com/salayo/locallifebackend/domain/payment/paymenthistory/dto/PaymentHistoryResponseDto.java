package com.salayo.locallifebackend.domain.payment.paymenthistory.dto;


import com.salayo.locallifebackend.domain.payment.enums.PaymentMethodType;
import com.salayo.locallifebackend.domain.payment.enums.PaymentProvider;
import com.salayo.locallifebackend.domain.payment.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PaymentHistoryResponseDto {

	private final Long id; //결제 내역 고유 식별자

	private final Long paymentId; //결제 고유 식별자

	private final Long memberId; //사용자 고유 식별자

	private final String merchantUid; //주문 고유 번호

	private final String pgTid; //PG사 거래 고유 ID

	private final String impUid; //Iamport Unique ID

	private final BigDecimal paymentCost; //결제 금액

	private final PaymentMethodType paymentMethodType; //결제 수단 타입

	private final PaymentProvider paymentProvider; //결제 대행사

	private final PaymentStatus paymentStatus; //결제 상태

	private final String refundReason; //환불 사유

	private final LocalDateTime refundedAt; //결제 환불(취소)일

	private final LocalDateTime refundFailedAt; //환불 실패일

	private final String refundFailedReason; //환불 실패 사유 - 내부 기록

	private final LocalDateTime paymentFailedAt; //결제 실패일

	private final String paymentFailedReason; //결제 실패 사유 - 내부 기록

	private final String pgFailMessage; //PG사 측 실패 메세지

	private final String paymentCardSnapshot; //카드 스냅샷

	private final BigDecimal totalRefundAmount; //최종 환불 금액

}
