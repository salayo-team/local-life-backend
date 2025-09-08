package com.salayo.locallifebackend.domain.payment.service;


import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.member.enums.MemberRole;
import com.salayo.locallifebackend.domain.member.repository.MemberRepository;
import com.salayo.locallifebackend.domain.payment.dto.PaymentCreateRequestDto;
import com.salayo.locallifebackend.domain.payment.dto.PaymentResponseDto;
import com.salayo.locallifebackend.domain.payment.entity.Payment;
import com.salayo.locallifebackend.domain.payment.enums.PaymentMethodType;
import com.salayo.locallifebackend.domain.payment.enums.PaymentProvider;
import com.salayo.locallifebackend.domain.payment.enums.PaymentStatus;
import com.salayo.locallifebackend.domain.payment.repository.PaymentRepository;
import com.salayo.locallifebackend.domain.paymenthistory.entity.PaymentHistory;
import com.salayo.locallifebackend.domain.paymenthistory.enums.PaymentHistoryStatus;
import com.salayo.locallifebackend.domain.paymenthistory.repository.PaymentHistoryRepository;
import com.salayo.locallifebackend.domain.reservation.entity.Reservation;
import com.salayo.locallifebackend.domain.reservation.enums.ReservationStatus;
import com.salayo.locallifebackend.domain.reservation.repository.ReservationRepository;
import com.salayo.locallifebackend.domain.review.enums.ReviewStatus;
import com.salayo.locallifebackend.global.enums.DeletedStatus;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.IamportResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class PaymentService {

	private final MemberRepository memberRepository;
	private final PaymentRepository paymentRepository;
	private final PaymentHistoryRepository paymentHistoryRepository;
	private final ReservationRepository reservationRepository;
	private final IamportClient iamportClient;

	public PaymentService(MemberRepository memberRepository, PaymentRepository paymentRepository,
		PaymentHistoryRepository paymentHistoryRepository, ReservationRepository reservationRepository, IamportClient iamportClient
	) {
		this.memberRepository = memberRepository;
		this.paymentRepository = paymentRepository;
		this.paymentHistoryRepository = paymentHistoryRepository;
		this.reservationRepository = reservationRepository;
		this.iamportClient = iamportClient;
	}

	/**
	 * 결제 생성 메서드
	 * - TODO : 예외처리 추가
	 * - TODO : 예약, 결제 만료 처리 스케줄러 구현
	 */
	@Transactional
	public PaymentResponseDto createPayment(Long memberId, Long reservationId, PaymentCreateRequestDto requestDto) {

		Member member = memberRepository.findByIdOrElseThrow(memberId);
		if (member.getMemberRole().equals(MemberRole.LOCAL_CREATOR)) {
			throw new CustomException(ErrorCode.RESERVATION_NOT_ALLOWED);
		}

		Reservation reservation = reservationRepository.findByIdOrElseThrow(reservationId);
		reservation.updateReservationStatus(ReservationStatus.PAYMENT_PENDING);

		String merchantUid = generateUniqueMerchantUid(reservation.getId());

		PaymentProvider paymentProvider = PaymentProvider.from(requestDto.getPaymentProvider())
			.orElse(null);

		PaymentMethodType paymentMethodType = PaymentMethodType.from(requestDto.getPaymentMethodType())
			.orElse(null);

		Payment payment = Payment.builder()
			.reservation(reservation)
			.merchantUid(merchantUid)
			.pgTid(requestDto.getPgTid())
			.impUid(requestDto.getImpUid())
			.paymentCost(requestDto.getPaymentCost())
			.paymentCard(requestDto.getPaymentCard())
			.paymentProvider(paymentProvider)
			.paymentMethodType(paymentMethodType)
			.paymentStatus(PaymentStatus.PAYMENT_PENDING)
			.deletedStatus(DeletedStatus.DISPLAYED)
			.build();

		paymentRepository.save(payment);

		return PaymentResponseDto.from(payment);
	}

	/**
	 * 주문 고유 번호 생성 메서드
	 * - 주문 번호 중복 체크를 위해 분리
	 * - TODO : 데이터 무결성 문제 될 경우 고민
	 */
	private String generateUniqueMerchantUid(Long reservationId) {

		String merchantUid;

		do {
			String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
			String randomStr = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
			merchantUid = "res-" + date + "-" + reservationId + "-" + randomStr;
		} while (paymentRepository.existsByMerchantUid(merchantUid));

		return merchantUid;
	}

	/**
	 * 결제 검증 메서드
	 * - 결제 검증 완료시 데이터 업데이트 & 결제 내역 생성
	 * - TODO : Throw 터졌을때, 실패 로그 저장되는 로직 수정하기
	 * - TODO : 검증 시, 특정 필드 null 허용 고민, 공백 방어 로직 추가하기
	 */
	@Transactional
	public PaymentResponseDto verifyPayment(Long memberId, Long paymentId, @Valid PaymentCreateRequestDto requestDto) {

		Member member = memberRepository.findByIdOrElseThrow(memberId);
		if (member.getMemberRole().equals(MemberRole.LOCAL_CREATOR)) {
			throw new CustomException(ErrorCode.RESERVATION_NOT_ALLOWED);
		}

		Payment payment = paymentRepository.findByIdOrElseThrow(paymentId);
		if (payment.getPaymentStatus() == PaymentStatus.PAYMENT_SUCCESS) {
			throw new CustomException(ErrorCode.PAYMENT_ALREADY_VERIFIED);
		}

		if (requestDto.getImpUid() == null || requestDto.getImpUid().isEmpty()) {
			throw new CustomException(ErrorCode.IMPUID_NOT_FOUND);
		}
		if (requestDto.getPgTid() == null || requestDto.getPgTid().isEmpty()) {
			throw new CustomException(ErrorCode.PGTID_NOT_FOUND);
		}

		IamportResponse<com.siot.IamportRestClient.response.Payment> iamportPaymentResponse;
		try {
			iamportPaymentResponse = iamportClient.paymentByImpUid(requestDto.getImpUid());
		} catch (IamportResponseException | IOException exception) {
			failAndThrow(payment, "iamport 결제 조회 실패", null,
				ErrorCode.IMPUID_NOT_FOUND_BY_IAMPORT);
			return null;
		}

		com.siot.IamportRestClient.response.Payment iamportPayment = iamportPaymentResponse.getResponse();

		if (iamportPayment == null || !"paid".equals(iamportPayment.getStatus())) {
			String pgFailMessage = iamportPayment != null ? iamportPayment.getFailReason() : null;
			failAndThrow(payment, "상태가 결제 완료가 아닙니다.", pgFailMessage,
				ErrorCode.PAYMENT_VERIFICATION_FAILED);
		}

		String iamportPgTidCheck = iamportPayment.getPgTid();
		if(iamportPgTidCheck == null){
			failAndThrow(payment, "iamport pgTid 값 없음", iamportPayment.getFailReason()
				, ErrorCode.PGTID_NOT_FOUND);
		}

		if (!requestDto.getPgTid().equals(iamportPayment.getPgTid())) {
			failAndThrow(payment, "pgTid가 일치하지 않습니다.", iamportPayment.getFailReason(),
				ErrorCode.PGTID_MISMATCH);
		}

		BigDecimal paymentCost = payment.getPaymentCost();
		BigDecimal iamportAmount = BigDecimal.valueOf(iamportPayment.getAmount().longValue());
		if (paymentCost.compareTo(iamportAmount) != 0) {
			failAndThrow(payment, "결제 금액이 일치하지 않습니다.", iamportPayment.getFailReason(),
				ErrorCode.PAYMENT_AMOUNT_MISMATCH);
		}

		if (!payment.getMerchantUid().equals(iamportPayment.getMerchantUid())) {
			failAndThrow(payment, "merchantUid 일치하지 않습니다.", iamportPayment.getFailReason(),
				ErrorCode.MERCHANT_UID_MISMATCH);
		}

		LocalDateTime paidAt = null;
		if (iamportPayment.getPaidAt() != null) {
			paidAt = LocalDateTime.ofInstant(
				iamportPayment.getPaidAt().toInstant(),
				ZoneId.of("Asia/Seoul")
			);
		}

		//결제 수단 enum 매핑
		PaymentMethodType paymentMethodType = PaymentMethodType.from(iamportPayment.getPayMethod())
			.orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_METHOD_MISMATCH));

		String cardSnapshot = cardSnapShotLabel(iamportPayment, requestDto.getPaymentCard());

		payment.updateCardSnapshot(cardSnapshot);

		payment.updatePayment(
			iamportPayment.getPgTid(),
			requestDto.getImpUid(),
			requestDto.getPaymentCard(),
			paymentMethodType
		);

		payment.verifySuccess(paidAt);

		Reservation reservation = payment.getReservation();
		if (reservation.getReservationStatus() != ReservationStatus.PAYMENT_PENDING) {
			throw new CustomException(ErrorCode.INVALID_RESERVATION_STATUS);
		}
		reservation.updateReservationStatus(ReservationStatus.PENDING);

		PaymentHistory paymentHistory = PaymentHistory.builder()
			.payment(payment)
			.member(member)
			.pgTid(iamportPayment.getPgTid())
			.impUid(iamportPayment.getImpUid())
			.paymentCost(iamportAmount)
			.paymentMethodType(paymentMethodType)
			.paymentProvider(payment.getPaymentProvider())
			.paymentHistoryStatus(PaymentHistoryStatus.PAYMENT_SUCCESS)
			.deletedStatus(DeletedStatus.DISPLAYED)
			.reviewStatus(ReviewStatus.NOT_YET)
			.paymentCardSnapshot(cardSnapshot)
			.build();
		paymentHistoryRepository.save(paymentHistory);

		return PaymentResponseDto.from(payment);
	}

	/**
	 * TODO : 실패 로그 저장 메서드 분리
	 * - 중복되는 로직 리팩토링
	 */
	private void failAndThrow(Payment payment, String paymentFailedReason, String iamportReason, ErrorCode errorCode) {
		payment.failPayment(paymentFailedReason, iamportReason);
		paymentRepository.save(payment);
		throw new CustomException(errorCode);
	}

	/**
	 * 카드 정보 스냅샷
	 * - 카드이름 공백 없이 추출
	 * - 카드번호 마지막 4자리만 추출
	 */
	private String cardSnapShotLabel(com.siot.IamportRestClient.response.Payment iamportPayment, String cardName) {

		String issuer = null;
		if (iamportPayment != null && iamportPayment.getCardName() != null && !iamportPayment.getCardName().isBlank()) {
			issuer = iamportPayment.getCardName().trim();
		} else if (cardName != null && !cardName.isBlank()) {
			issuer = cardName.trim();
		}

		if (issuer != null && !issuer.isEmpty()) {
			issuer = issuer.replaceAll("\\s+", "");
		}

		String lastFourNumber = null;
		if (iamportPayment != null && iamportPayment.getCardNumber() != null && !iamportPayment.getCardNumber().isBlank()) {

			String cardNumbers = iamportPayment.getCardNumber().trim().replaceAll("\\D", "");
			if (cardNumbers.length() >= 4) {
				lastFourNumber = cardNumbers.substring(cardNumbers.length() - 4);
			}
		}

		if (issuer == null || lastFourNumber == null) {
			return null;
		}

		return issuer + "_" + lastFourNumber;
	}


}
