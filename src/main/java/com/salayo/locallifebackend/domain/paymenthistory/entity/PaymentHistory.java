package com.salayo.locallifebackend.domain.paymenthistory.entity;

import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.payment.entity.Payment;
import com.salayo.locallifebackend.domain.paymenthistory.enums.PaymentHistoryStatus;
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
import jakarta.persistence.ManyToOne;
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
@Table(name = "payment_history")
public class PaymentHistory extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; //결제 내역 고유 식별자

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_id", nullable = false)
	private Payment payment; //결제 고유 식별자

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private Member member; //사용자 고유 식별자

	@Column(nullable = false, length = 100)
	private String pgTid; //PG사 거래 고유 ID

	@Column(nullable = false, length = 100)
	private String impUid; //Iamport Unique ID

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal paymentCost; //결제 금액

	@Column(nullable = false, length = 50)
	private String paymentCard; //결제 카드 정보

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private PaymentHistoryStatus paymentHistoryStatus; //결제 내역 상태

	@Column(nullable = true, columnDefinition = "TEXT")
	private String refundReason; //환불 사유

	@Column(nullable = true)
	private LocalDateTime canceledAt; //결제 취소일

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private DeletedStatus deletedStatus; //결제 내역 삭제 상태
	
	//TODO : ReviewStatus 추가하기
	

	@Builder
	public PaymentHistory(Payment payment, Member member, String pgTid, String impUid, BigDecimal paymentCost, String paymentCard,
		PaymentHistoryStatus paymentHistoryStatus, String refundReason, LocalDateTime canceledAt, DeletedStatus deletedStatus) {
		this.payment = payment;
		this.member = member;
		this.pgTid = pgTid;
		this.impUid = impUid;
		this.paymentCost = paymentCost;
		this.paymentCard = paymentCard;
		this.paymentHistoryStatus = paymentHistoryStatus;
		this.refundReason = refundReason;
		this.canceledAt = canceledAt;
		this.deletedStatus = deletedStatus;
	}
}
