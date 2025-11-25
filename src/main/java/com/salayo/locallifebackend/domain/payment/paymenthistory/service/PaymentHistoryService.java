package com.salayo.locallifebackend.domain.payment.paymenthistory.service;

import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.payment.entity.Payment;
import com.salayo.locallifebackend.domain.payment.paymenthistory.entity.PaymentHistory;
import com.salayo.locallifebackend.domain.payment.paymenthistory.repository.PaymentHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class PaymentHistoryService {

	private final PaymentHistoryRepository paymentHistoryRepository;

	public PaymentHistoryService(PaymentHistoryRepository paymentHistoryRepository) {
		this.paymentHistoryRepository = paymentHistoryRepository;
	}

	/**
	 * 결제 실패 이력 저장
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void savePaymentFailureHistory(Payment payment, Member member, String paymentFailedReason, String pgFailMessage) {

		PaymentHistory paymentHistory = PaymentHistory.createFailurePaymentHistory(
			payment,
			member,
			paymentFailedReason,
			pgFailMessage
		);
		paymentHistoryRepository.save(paymentHistory);
	}

}
