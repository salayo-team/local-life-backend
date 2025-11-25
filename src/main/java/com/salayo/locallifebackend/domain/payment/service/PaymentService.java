package com.salayo.locallifebackend.domain.payment.service;


import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.member.enums.MemberRole;
import com.salayo.locallifebackend.domain.member.repository.MemberRepository;
import com.salayo.locallifebackend.domain.payment.dto.PaymentRequestDto;
import com.salayo.locallifebackend.domain.payment.dto.PaymentResponseDto;
import com.salayo.locallifebackend.domain.payment.entity.Payment;
import com.salayo.locallifebackend.domain.payment.enums.PaymentMethodType;
import com.salayo.locallifebackend.domain.payment.enums.PaymentProvider;
import com.salayo.locallifebackend.domain.payment.enums.PaymentStatus;
import com.salayo.locallifebackend.domain.payment.paymenthistory.service.PaymentHistoryService;
import com.salayo.locallifebackend.domain.payment.repository.PaymentRepository;
import com.salayo.locallifebackend.domain.payment.paymenthistory.entity.PaymentHistory;
import com.salayo.locallifebackend.domain.payment.paymenthistory.repository.PaymentHistoryRepository;
import com.salayo.locallifebackend.domain.reservation.entity.Reservation;
import com.salayo.locallifebackend.domain.reservation.enums.ReservationStatus;
import com.salayo.locallifebackend.domain.reservation.repository.ReservationRepository;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.IamportResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
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
	private final PaymentHistoryService paymentHistoryService;

	public PaymentService(MemberRepository memberRepository, PaymentRepository paymentRepository,
		PaymentHistoryRepository paymentHistoryRepository, ReservationRepository reservationRepository, IamportClient iamportClient,
		PaymentHistoryService paymentHistoryService
	) {
		this.memberRepository = memberRepository;
		this.paymentRepository = paymentRepository;
		this.paymentHistoryRepository = paymentHistoryRepository;
		this.reservationRepository = reservationRepository;
		this.iamportClient = iamportClient;
		this.paymentHistoryService = paymentHistoryService;
	}

	/**
	 * 결제 생성 메서드
	 * - TODO : 예외처리 추가
	 * - TODO : 예약, 결제 만료 처리 스케줄러 구현
	 */
	@Transactional
	public PaymentResponseDto createPayment(Long memberId, Long reservationId, PaymentRequestDto requestDto) {

		Member member = memberRepository.findByIdOrElseThrow(memberId);
		if (member.getMemberRole().equals(MemberRole.LOCAL_CREATOR)) {
			throw new CustomException(ErrorCode.PAYMENT_NOT_ALLOWED);
		}

		Reservation reservation = reservationRepository.findByIdOrElseThrow(reservationId);
		if (reservation.getReservationStatus() != ReservationStatus.REQUESTED) {
			throw new CustomException(ErrorCode.INVALID_RESERVATION_STATUS);
		}
		reservation.changeToPaymentPending();

		String merchantUid = generateUniqueMerchantUid(reservation.getId());

		PaymentProvider paymentProvider = PaymentProvider.from(requestDto.getPaymentProvider())
			.orElseThrow(() -> new CustomException(ErrorCode.INVALID_PAYMENT_PROVIDER));

		PaymentMethodType paymentMethodType = PaymentMethodType.from(requestDto.getPaymentMethodType())
			.orElseThrow(() -> new CustomException(ErrorCode.INVALID_PAYMENT_METHODTYPE));

		Payment payment = Payment.preparePayment(
			reservation,
			merchantUid,
			requestDto.getPgTid(),
			requestDto.getImpUid(),
			requestDto.getPaymentCost(),
			requestDto.getPaymentCard(),
			paymentProvider,
			paymentMethodType
		);
		paymentRepository.save(payment);

		return PaymentResponseDto.from(payment);
	}

	/**
	 * 주문 고유 번호 생성 메서드
	 * - 주문 번호 중복 체크를 위해 분리
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
	 */
	@Transactional
	public PaymentResponseDto verifyPayment(Long memberId, Long paymentId, PaymentRequestDto requestDto) {

		Member member = memberRepository.findByIdOrElseThrow(memberId);
		if (member.getMemberRole().equals(MemberRole.LOCAL_CREATOR)) {
			throw new CustomException(ErrorCode.PAYMENT_NOT_ALLOWED);
		}

		Payment payment = paymentRepository.findByIdOrElseThrow(paymentId);
		if (payment.getPaymentStatus() == PaymentStatus.PAYMENT_SUCCESS) {
			throw new CustomException(ErrorCode.PAYMENT_ALREADY_VERIFIED);
		}

		Reservation reservation = payment.getReservation();
		if (reservation.getReservationStatus() != ReservationStatus.PAYMENT_PENDING) {
			throw new CustomException(ErrorCode.INVALID_RESERVATION_STATUS);
		}

		if (requestDto.getImpUid() == null || requestDto.getImpUid().isEmpty()) {
			throw new CustomException(ErrorCode.IMP_UID_NOT_FOUND);
		}
		if (requestDto.getPgTid() == null || requestDto.getPgTid().isEmpty()) {
			throw new CustomException(ErrorCode.PG_TID_NOT_FOUND);
		}

		IamportResponse<com.siot.IamportRestClient.response.Payment> iamportPaymentResponse;
		try {
			iamportPaymentResponse = iamportClient.paymentByImpUid(requestDto.getImpUid());
		} catch (IamportResponseException | IOException exception) {
			throw failPayment(
				payment,
				member,
				"impUid 값으로 portOne 결제 조회 실패"
					+ "[impUid 값 : " + requestDto.getImpUid() + " ]",
				null,
				ErrorCode.IMP_UID_NOT_FOUND_BY_IAMPORT);
		}

		com.siot.IamportRestClient.response.Payment iamportPayment = iamportPaymentResponse.getResponse();

		if (iamportPayment == null || !"paid".equals(iamportPayment.getStatus())) {
			String pgFailMessage = iamportPayment != null ? iamportPayment.getFailReason() : null;
			throw failPayment(
				payment,
				member,
				"portOne 응답 데이터 상태가 결제 완료가 아닙니다.",
				pgFailMessage,
				ErrorCode.PAYMENT_VERIFICATION_FAILED);
		}

		String iamportImpUid = iamportPayment.getImpUid();
		if (!requestDto.getImpUid().equals(iamportImpUid)) {
			throw failPayment(
				payment,
				member,
				"portOne 응답 데이터와 impUid가 일치하지 않습니다."
					+ "[PortOne 응답 값 : " + iamportImpUid + "| Dto 값 : " + requestDto.getImpUid() + " ]",
				iamportPayment.getFailReason(),
				ErrorCode.IMP_UID_MISMATCH);
		}

		String iamportPgTid = iamportPayment.getPgTid();
		if (iamportPgTid == null) {
			throw failPayment(
				payment,
				member,
				"portOne 응답 데이터에 pgTid 값 없음",
				iamportPayment.getFailReason()
				, ErrorCode.PG_TID_NOT_FOUND);
		}

		if (!requestDto.getPgTid().equals(iamportPgTid)) {
			throw failPayment(
				payment,
				member,
				"portOne 응답 데이터와 pgTid가 일치하지 않습니다."
					+ "[PortOne 응답 값 : " + iamportPgTid + "| Dto 값 : " + requestDto.getPgTid() + " ]",
				iamportPayment.getFailReason(),
				ErrorCode.PG_TID_MISMATCH);
		}

		BigDecimal paymentCost = payment.getPaymentCost();
		BigDecimal iamportAmount = iamportPayment.getAmount();
		if (paymentCost.compareTo(iamportAmount) != 0) {
			throw failPayment(
				payment,
				member,
				"portOne 응답 데이터와 DB에 저장된 결제 금액이 일치하지 않습니다."
					+ "[PortOne 응답 값 : " + iamportAmount + "| DB 값 : " + paymentCost + " ]",
				iamportPayment.getFailReason(),
				ErrorCode.PAYMENT_AMOUNT_MISMATCH);
		}

		String iamportMerchantUid = iamportPayment.getMerchantUid();
		if (!payment.getMerchantUid().equals(iamportMerchantUid)) {
			throw failPayment(
				payment,
				member,
				"portOne 응답 데이터와 DB에 저장된 merchantUid가 일치하지 않습니다."
					+ "[PortOne 응답 값 : " + iamportMerchantUid + "| DB 값 : " + payment.getMerchantUid() + " ]",
				iamportPayment.getFailReason(),
				ErrorCode.MERCHANT_UID_MISMATCH);
		}

		LocalDateTime paidAt = null;
		if (iamportPayment.getPaidAt() != null) {
			paidAt = LocalDateTime.ofInstant(
				iamportPayment.getPaidAt().toInstant(),
				ZoneId.of("Asia/Seoul")
			);
		}

		String iamportPaymentMethodTypeStr = iamportPayment.getPayMethod();
		Optional<PaymentMethodType> paymentMethodType = PaymentMethodType.from(iamportPaymentMethodTypeStr);
		if (paymentMethodType.isEmpty()) {
			throw failPayment(
				payment,
				member,
				"지원하지 않거나, 올바르지 않은 결제 수단입니다. "
					+ "[PortOne 응답 값 : " + iamportPaymentMethodTypeStr + " ]",
				iamportPayment.getFailReason(), ErrorCode.INVALID_PAYMENT_METHODTYPE);
		}

		PaymentMethodType iamportPaymentMethodType = paymentMethodType.get();
		if (!payment.getPaymentMethodType().equals(iamportPaymentMethodType)) {
			throw failPayment(
				payment,
				member,
				"portOne 응답 데이터와 DB에 저장된 결제 수단이 일치하지 않습니다. "
					+ "[PortOne 응답 값 : " + iamportPaymentMethodType + "| DB 값 : " + payment.getPaymentMethodType() + " ]",
				iamportPayment.getFailReason(),
				ErrorCode.PAYMENT_METHOD_MISMATCH
			);
		}

		Optional<PaymentProvider> paymentProvider = PaymentProvider.from(iamportPayment.getPgProvider());
		if (paymentProvider.isEmpty()) {
			throw failPayment(
				payment,
				member,
				"지원하지 않거나, 올바르지 않은 결제 대행사입니다. [PortOne 응답 값] : " + iamportPayment.getPgProvider(),
				iamportPayment.getFailReason(),
				ErrorCode.INVALID_PAYMENT_PROVIDER
			);
		}

		PaymentProvider iamportPaymentProvider = paymentProvider.get();
		if (!payment.getPaymentProvider().equals(iamportPaymentProvider)) {
			throw failPayment(
				payment,
				member,
				"portOne 응답 데이터와 DB에 저장된 결제 대행사가 일치하지 않습니다."
					+ "[PortOne 응답 값 : " + iamportPaymentProvider + "| DB 값 : " + payment.getPaymentProvider() + " ]",
				iamportPayment.getPgProvider(),
				ErrorCode.PAYMENT_PROVIDER_MISMATCH
			);
		}

		String cardSnapshot = cardSnapShotLabel(iamportPayment, requestDto.getPaymentCard());
		payment.updateCardSnapshot(cardSnapshot);

		payment.updatePayment(
			iamportPgTid,
			iamportImpUid,
			requestDto.getPaymentCard(),
			iamportPaymentMethodType
		);

		payment.verifySuccess(paidAt);

		reservation.changeToAwaitingApproval();

		PaymentHistory paymentHistory = PaymentHistory.createSuccessPaymentHistory(
			payment,
			member,
			cardSnapshot
		);
		paymentHistoryRepository.save(paymentHistory);

		return PaymentResponseDto.from(payment);
	}

	/**
	 * - 결제 실패 처리 메서드
	 * - 결제 실패시 이력 저장 & 예외 생성
	 */
	private CustomException failPayment(Payment payment, Member member, String paymentFailedReason, String pgFailMessage,
		ErrorCode errorCode) {

		paymentHistoryService.savePaymentFailureHistory(payment, member, paymentFailedReason, pgFailMessage);
		return new CustomException(errorCode);
	}

	/**
	 * 카드 정보 스냅샷
	 * - 카드이름 공백 없이 추출
	 * - 카드번호 마지막 4자리만 추출
	 */
	private String cardSnapShotLabel(com.siot.IamportRestClient.response.Payment iamportPayment, String cardName) {

		String issuer = null;
		if (iamportPayment.getCardName() != null && !iamportPayment.getCardName().isBlank()) {
			issuer = iamportPayment.getCardName().trim();
		} else if (cardName != null && !cardName.isBlank()) {
			issuer = cardName.trim();
		}

		if (issuer != null) {
			issuer = issuer.replaceAll("\\s+", "");
		}

		String lastFourNumber = null;
		if (iamportPayment.getCardNumber() != null && !iamportPayment.getCardNumber().isBlank()) {
			String cardNumbers = iamportPayment.getCardNumber().replaceAll("\\D", "");

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
