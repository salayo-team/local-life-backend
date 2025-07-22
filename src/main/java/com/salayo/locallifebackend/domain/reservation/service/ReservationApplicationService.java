package com.salayo.locallifebackend.domain.reservation.service;

import com.salayo.locallifebackend.domain.member.repository.MemberRepository;
import com.salayo.locallifebackend.domain.payment.entity.Payment;
import com.salayo.locallifebackend.domain.payment.service.PaymentService;
import com.salayo.locallifebackend.domain.reservation.dto.ReservationAndPaymentRequestDto;
import com.salayo.locallifebackend.domain.reservation.dto.ReservationAndPaymentResponseDto;
import com.salayo.locallifebackend.domain.reservation.entity.Reservation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationApplicationService {

	private final MemberRepository memberRepository;
	private final ReservationService reservationService;
	private final PaymentService paymentService;

	public ReservationApplicationService(MemberRepository memberRepository, ReservationService reservationService,
		PaymentService paymentService) {
		this.memberRepository = memberRepository;
		this.reservationService = reservationService;
		this.paymentService = paymentService;
	}

	/**
	 * 예약 생성 & 결제 생성 호출 메서드
	 * - TODO : PG api 호출 실패 예외 처리 - Custom Exception class 생성
	 * - TODO : 결제 실패 로직 추가
	 * - TODO : 예약, 결제 만료 상태 변경 & 시점 기록
	 */
	@Transactional
	public ReservationAndPaymentResponseDto createReservationAndPayment(Long memberId, ReservationAndPaymentRequestDto requestDto) {

		Reservation reservation = reservationService.createReservation(memberId, requestDto.getReservationCreateRequestDto());

		Payment payment = paymentService.createPayment(reservation, requestDto.getPaymentCreateRequestDto());

		//TODO : 결제 검증

		//TODO : 결제 내역 생성

		return ReservationAndPaymentResponseDto.from(reservation, payment);

	}
}
