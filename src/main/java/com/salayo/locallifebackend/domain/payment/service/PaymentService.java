package com.salayo.locallifebackend.domain.payment.service;


import com.salayo.locallifebackend.domain.payment.dto.PaymentCreateRequestDto;
import com.salayo.locallifebackend.domain.payment.entity.Payment;
import com.salayo.locallifebackend.domain.payment.enums.PaymentStatus;
import com.salayo.locallifebackend.domain.payment.repository.PaymentRepository;
import com.salayo.locallifebackend.domain.reservation.entity.Reservation;
import com.salayo.locallifebackend.domain.reservation.enums.ReservationStatus;
import com.salayo.locallifebackend.global.enums.DeletedStatus;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class PaymentService {

	private final PaymentRepository paymentRepository;

	public PaymentService(PaymentRepository paymentRepository) {
		this.paymentRepository = paymentRepository;
	}

	/**
	 * 결제 생성 메서드
	 * - TODO : 결제 예외, 검증 추가
	 * - TODO : 예약, 결제 만료 처리 스케줄러 구현
	 */
	@Transactional
	public Payment createPayment(Reservation reservation, PaymentCreateRequestDto requestDto) {

		reservation.updateReservationStatus(ReservationStatus.PAYMENT_PENDING);

		String merchantUid = generateUniqueMerchantUid(reservation.getId());

		Payment payment = Payment.builder()
			.reservation(reservation)
			.merchantUid(merchantUid)
			.pgTid(requestDto.getPgTid())
			.impUid(requestDto.getImpUid())
			.paymentCost(requestDto.getPaymentCost())
			.paymentCard(requestDto.getPaymentCard())
			.paymentMethodType(requestDto.getPaymentMethodType())
			.paymentStatus(PaymentStatus.PAYMENT_PENDING)
			.deletedStatus(DeletedStatus.DISPLAYED)
			.build();

		paymentRepository.save(payment);

		return payment;
	}

	/**
	 * 주문 고유 번호 생성 메서드
	 * - 주문 번호 중복 체크를 위해 분리
	 */
	private String generateUniqueMerchantUid(Long reservationId) {

		String merchantUid;

		do {
			merchantUid = "res-" + reservationId + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
		} while (paymentRepository.existsByMerchantUid(merchantUid));

		return merchantUid;
	}

}
