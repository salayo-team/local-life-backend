package com.salayo.locallifebackend.domain.payment.paymenthistory.repository;

import com.salayo.locallifebackend.domain.payment.paymenthistory.entity.PaymentHistory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {

	Optional<PaymentHistory> findByPaymentId(Long paymentId);

	Optional<PaymentHistory> findByPaymentIdAndMemberId(Long paymentId, Long memberId);

}
