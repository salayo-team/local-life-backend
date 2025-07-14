package com.salayo.locallifebackend.domain.paymenthistory.repository;

import com.salayo.locallifebackend.domain.paymenthistory.entity.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {

}
