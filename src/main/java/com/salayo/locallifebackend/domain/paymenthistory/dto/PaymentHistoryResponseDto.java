package com.salayo.locallifebackend.domain.paymenthistory.dto;


import com.salayo.locallifebackend.domain.paymenthistory.enums.PaymentHistoryStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PaymentHistoryResponseDto {

	private Long id; //결제 내역 고유 식별자

	private Long paymentId; //결제 고유 식별자

	private Long userId; //사용자 고유 식별자

	private String pgTid; //PG사 거래 고유 ID

	private String impUid; //Iamport Unique ID

	private BigDecimal paymentCost; //결제 금액

	private String paymentCard; //결제 카드 정보

	private PaymentHistoryStatus paymentHistoryStatus; //결제 내역 상태

	private String refundReason; //환불 사유

	private LocalDateTime canceledAt; //결제 취소일

	private LocalDateTime createdAt; //결제 내역 생성일

	private LocalDateTime modifiedAt; //결제 내역 수정일

	//TODO : 리뷰 작성 상태 필드 추가

}
