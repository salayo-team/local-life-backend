package com.salayo.locallifebackend.domain.payment.paymenthistory.service;

import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.payment.entity.Payment;
import com.salayo.locallifebackend.domain.payment.paymenthistory.entity.PaymentHistory;
import com.salayo.locallifebackend.domain.payment.paymenthistory.repository.PaymentHistoryRepository;
import com.salayo.locallifebackend.domain.payment.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class PaymentHistoryService {

	private final PaymentHistoryRepository paymentHistoryRepository;
	private final PaymentRepository paymentRepository;

	public PaymentHistoryService(PaymentHistoryRepository paymentHistoryRepository, PaymentRepository paymentRepository) {
		this.paymentHistoryRepository = paymentHistoryRepository;
		this.paymentRepository = paymentRepository;
	}

	/**
	 * 결제 실패 이력 저장
	 * - Payment entity 같이 update : 마지막 결제 실패일 + 결제 실패 카운트
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void savePaymentFailureHistory(Payment payment, Member member, String paymentFailedReason, String pgFailMessage) {

		Payment updatePayment = paymentRepository.findByIdOrElseThrow(payment.getId());
		updatePayment.failPayment();

		PaymentHistory paymentHistory = PaymentHistory.createPaymentFailurePaymentHistory(
			updatePayment,
			member,
			paymentFailedReason,
			pgFailMessage
		);
		paymentHistoryRepository.save(paymentHistory);
	}

	/**
	 * 환불 실패 이력 저장
	 * - Payment entity 같이 update : 마지막 환불 실패일 + 환불 실패 카운트
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void saveRefundFailureHistory(Payment payment, Member member, String refundFailedReason, String pgFailMessage) {

		Payment updatePayment = paymentRepository.findByIdOrElseThrow(payment.getId());
		updatePayment.failRefund();

		PaymentHistory paymentHistory = PaymentHistory.createRefundFailurePaymentHistory(
			updatePayment,
			member,
			refundFailedReason,
			pgFailMessage
		);
		paymentHistoryRepository.save(paymentHistory);
	}

}
