package com.salayo.locallifebackend.domain.payment.repository;

import com.salayo.locallifebackend.domain.payment.entity.Payment;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

	boolean existsByMerchantUid(String merchantUid);

	default Payment findByIdOrElseThrow(Long paymentId) {
		return findById(paymentId).orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
	}

}
