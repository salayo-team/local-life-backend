package com.salayo.locallifebackend.domain.reservation.service;

import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.member.enums.MemberRole;
import com.salayo.locallifebackend.domain.member.repository.MemberRepository;
import com.salayo.locallifebackend.domain.payment.dto.PaymentRefundRequestDto;
import com.salayo.locallifebackend.domain.payment.entity.Payment;
import com.salayo.locallifebackend.domain.payment.repository.PaymentRepository;
import com.salayo.locallifebackend.domain.payment.service.PaymentService;
import com.salayo.locallifebackend.domain.reservation.dto.ReservationCancelRequestDto;
import com.salayo.locallifebackend.domain.reservation.dto.ReservationRejectRequestDto;
import com.salayo.locallifebackend.domain.reservation.dto.ReservationResponseDto;
import com.salayo.locallifebackend.domain.reservation.entity.Reservation;
import com.salayo.locallifebackend.domain.reservation.enums.ReservationStatus;
import com.salayo.locallifebackend.domain.reservation.repository.ReservationRepository;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ReservationAndPaymentApplicationService {

	private final PaymentService paymentService;
	private final ReservationRepository reservationRepository;
	private final PaymentRepository paymentRepository;
	private final MemberRepository memberRepository;

	public ReservationAndPaymentApplicationService(PaymentService paymentService,
		ReservationRepository reservationRepository, PaymentRepository paymentRepository, MemberRepository memberRepository) {
		this.paymentService = paymentService;
		this.reservationRepository = reservationRepository;
		this.paymentRepository = paymentRepository;
		this.memberRepository = memberRepository;
	}

	/**
	 * 예약 취소 & 환불 메서드
	 * - 사용자가 예약 취소시 결제 환불 진행
	 */
	@Transactional
	public ReservationResponseDto cancelReservationWithRefund(Long memberId, Long reservationId, ReservationCancelRequestDto requestDto) {

		Member member = memberRepository.findActiveByIdOrThrow(memberId);
		if (member.getMemberRole() != MemberRole.USER) {
			throw new CustomException(ErrorCode.RESERVATION_CANCEL_NOT_ALLOWED);
		}

		Reservation reservation = reservationRepository.findByIdOrElseThrow(reservationId);

		Long reservationOwner = reservation.getMember().getId();
		if (!reservationOwner.equals(memberId)) {
			throw new CustomException(ErrorCode.RESERVATION_NOT_FOUND_FOR_USER);
		}

		Set<ReservationStatus> cancelableStatuses = ReservationStatus.memberCancelableStatusSet();
		if (!cancelableStatuses.contains(reservation.getReservationStatus())) {
			throw new CustomException(ErrorCode.RESERVATION_NOT_CANCELABLE);
		}

		Payment payment = paymentRepository.findByReservationId(reservationId)
			.orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

		PaymentRefundRequestDto paymentRefundRequestDto = PaymentRefundRequestDto.builder()
			.refundReason(requestDto.getCancelReason())
			.build();
		paymentService.refundPayment(payment.getId(), paymentRefundRequestDto);

		reservation.cancelReservation(requestDto.getCancelReason());

		return ReservationResponseDto.from(reservation);
	}

	/**
	 * 예약 거절 & 환불 메서드
	 * - 로컬 크리에이터가 예약(신청) 거절시 결제 환불 진행
	 */
	@Transactional
	public ReservationResponseDto rejectReservationWithRefund(Long memberId, Long reservationId, ReservationRejectRequestDto requestDto) {

		Member member = memberRepository.findActiveByIdOrThrow(memberId);
		if (member.getMemberRole() != MemberRole.LOCAL_CREATOR) {
			throw new CustomException(ErrorCode.RESERVATION_REJECT_NOT_ALLOWED);
		}

		Reservation reservation = reservationRepository.findByIdOrElseThrow(reservationId);

		Long programOwner = reservation.getProgramSchedule().getProgram().getMember().getId();
		if (!programOwner.equals(memberId)) {
			throw new CustomException(ErrorCode.PROGRAM_NOT_FOUND_FOR_USER);
		}

		Set<ReservationStatus> rejectableStatus = ReservationStatus.localCreatorRejectableStatusSet();
		if (!rejectableStatus.contains(reservation.getReservationStatus())) {
			throw new CustomException(ErrorCode.RESERVATION_NOT_REJECTABLE);
		}

		Payment payment = paymentRepository.findByReservationId(reservationId)
			.orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

		PaymentRefundRequestDto paymentRefundRequestDto = PaymentRefundRequestDto.builder()
			.refundReason(requestDto.getRejectedReason())
			.build();
		paymentService.refundPayment(payment.getId(), paymentRefundRequestDto);

		reservation.rejectReservation(requestDto.getRejectedReason());

		return ReservationResponseDto.from(reservation);
	}


}
