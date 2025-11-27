package com.salayo.locallifebackend.domain.payment.service;


import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.member.enums.MemberRole;
import com.salayo.locallifebackend.domain.member.repository.MemberRepository;
import com.salayo.locallifebackend.domain.payment.dto.PaymentRefundRequestDto;
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
import com.salayo.locallifebackend.domain.programschedule.entity.ProgramSchedule;
import com.salayo.locallifebackend.domain.reservation.entity.Reservation;
import com.salayo.locallifebackend.domain.reservation.enums.ReservationStatus;
import com.salayo.locallifebackend.domain.reservation.repository.ReservationRepository;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.IamportResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;

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
	 * - TODO : 예약, 결제 만료 처리 스케줄러 구현
	 */
	@Transactional
	public PaymentResponseDto createPayment(Long memberId, Long reservationId, PaymentRequestDto requestDto) {

		Member member = memberRepository.findActiveByIdOrThrow(memberId);
		if (member.getMemberRole() == MemberRole.LOCAL_CREATOR) {
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
	 * - TODO : 결제 실패 발생시 자동 환불
	 */
	@Transactional
	public PaymentResponseDto verifyPayment(Long memberId, Long paymentId, PaymentRequestDto requestDto) {

		Member member = memberRepository.findActiveByIdOrThrow(memberId);
		if (member.getMemberRole() == MemberRole.LOCAL_CREATOR) {
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

			String pgFailMessage =
				"PortOne 결제 검증 실패 - IamportResponseException 발생";
			throw failPayment(
				payment,
				member,
				"impUid 값으로 portOne 결제 조회 실패"
					+ "[impUid 값 : " + requestDto.getImpUid() + " ]",
				pgFailMessage,
				ErrorCode.IMP_UID_NOT_FOUND_BY_IAMPORT
			);
		}

		com.siot.IamportRestClient.response.Payment iamportPayment = iamportPaymentResponse.getResponse();

		if (iamportPayment == null) {

			String pgFailMessage =
				"PortOne 결제 검증 실패 : " + "\n"
					+ " code=[" + iamportPaymentResponse.getCode() + "]" + "\n"
					+ " message=[" + iamportPaymentResponse.getMessage() + "]";
			throw failPayment(
				payment,
				member,
				"portOne 응답 데이터 null 입니다.",
				pgFailMessage,
				ErrorCode.PAYMENT_VERIFICATION_FAILED
			);
		}

		if (!"paid".equalsIgnoreCase(iamportPayment.getStatus())) {

			String pgFailMessage =
				"PortOne 결제 검증 실패 - 응답 데이터 상태 : " + "\n"
					+ " code=[" + iamportPaymentResponse.getCode() + "]" + "\n"
					+ " status=[" + iamportPayment.getStatus() + "]" + "\n"
					+ " message=[" + iamportPaymentResponse.getMessage() + "]" + "\n"
					+ " amount=[" + iamportPayment.getAmount() + "]" + "\n"
					+ " failReason=[" + iamportPayment.getFailReason() + "]";
			throw failPayment(
				payment,
				member,
				"portOne 응답 데이터 상태가 결제 완료가 아닙니다.",
				pgFailMessage,
				ErrorCode.PAYMENT_VERIFICATION_FAILED
			);
		}

		String iamportImpUid = iamportPayment.getImpUid();
		if (!requestDto.getImpUid().equals(iamportImpUid)) {

			String pgFailMessage =
				"PortOne 결제 검증 실패 - 응답 데이터 상태 : " + "\n"
					+ " code=[" + iamportPaymentResponse.getCode() + "]" + "\n"
					+ " status=[" + iamportPayment.getStatus() + "]" + "\n"
					+ " message=[" + iamportPaymentResponse.getMessage() + "]" + "\n"
					+ " failReason=[" + iamportPayment.getFailReason() + "]";
			throw failPayment(
				payment,
				member,
				"portOne 응답 데이터와 impUid가 일치하지 않습니다." + "\n"
					+ "[PortOne 응답 값 : " + iamportImpUid + "\n"
					+ " | Dto 값 : " + requestDto.getImpUid() + " ]",
				pgFailMessage,
				ErrorCode.IMP_UID_MISMATCH
			);
		}

		String iamportPgTid = iamportPayment.getPgTid();
		if (iamportPgTid == null) {

			String pgFailMessage =
				"PortOne 결제 검증 실패 - 응답 데이터 상태 : " + "\n"
					+ " code=[" + iamportPaymentResponse.getCode() + "]" + "\n"
					+ " status=[" + iamportPayment.getStatus() + "]" + "\n"
					+ " message=[" + iamportPaymentResponse.getMessage() + "]" + "\n"
					+ " failReason=[" + iamportPayment.getFailReason() + "]";
			throw failPayment(
				payment,
				member,
				"portOne 응답 데이터에 pgTid 값 없음",
				pgFailMessage
				, ErrorCode.PG_TID_NOT_FOUND
			);
		}

		if (!requestDto.getPgTid().equals(iamportPgTid)) {

			String pgFailMessage =
				"PortOne 결제 검증 실패 - 응답 데이터 상태 : " + "\n"
					+ " code=[" + iamportPaymentResponse.getCode() + "]" + "\n"
					+ " status=[" + iamportPayment.getStatus() + "]" + "\n"
					+ " message=[" + iamportPaymentResponse.getMessage() + "]" + "\n"
					+ " failReason=[" + iamportPayment.getFailReason() + "]";
			throw failPayment(
				payment,
				member,
				"portOne 응답 데이터와 pgTid가 일치하지 않습니다." + "\n"
					+ "[PortOne 응답 값 : " + iamportPgTid + "\n"
					+ " | Dto 값 : " + requestDto.getPgTid() + " ]",
				pgFailMessage,
				ErrorCode.PG_TID_MISMATCH
			);
		}

		BigDecimal paymentCost = payment.getPaymentCost();
		BigDecimal iamportAmount = iamportPayment.getAmount();
		if (iamportAmount == null) {

			String pgFailMessage =
				"PortOne 결제 검증 실패 - 응답 데이터 상태 : " + "\n"
					+ " code=[" + iamportPaymentResponse.getCode() + "]" + "\n"
					+ " status=[" + iamportPayment.getStatus() + "]" + "\n"
					+ " message=[" + iamportPaymentResponse.getMessage() + "]" + "\n"
					+ " failReason=[" + iamportPayment.getFailReason() + "]";
			throw failPayment(
				payment,
				member,
				"portOne 응답 데이터에 결제 금액이 없습니다.(null 값)" + "\n"
					+ "[PortOne 응답 값 : " + iamportAmount + "\n"
					+ " | DB 값 : " + paymentCost + " ]",
				pgFailMessage,
				ErrorCode.PAYMENT_AMOUNT_MISMATCH
			);
		}

		if (paymentCost.compareTo(iamportAmount) != 0) {

			String pgFailMessage =
				"PortOne 결제 검증 실패 - 응답 데이터 상태 : " + "\n"
					+ " code=[" + iamportPaymentResponse.getCode() + "]" + "\n"
					+ " status=[" + iamportPayment.getStatus() + "]" + "\n"
					+ " message=[" + iamportPaymentResponse.getMessage() + "]" + "\n"
					+ " failReason=[" + iamportPayment.getFailReason() + "]";
			throw failPayment(
				payment,
				member,
				"portOne 응답 데이터와 DB에 저장된 결제 금액이 일치하지 않습니다." + "\n"
					+ "[PortOne 응답 값 : " + iamportAmount + "\n"
					+ " | DB 값 : " + paymentCost + " ]",
				pgFailMessage,
				ErrorCode.PAYMENT_AMOUNT_MISMATCH
			);
		}

		String iamportMerchantUid = iamportPayment.getMerchantUid();
		if (!payment.getMerchantUid().equals(iamportMerchantUid)) {

			String pgFailMessage =
				"PortOne 결제 검증 실패 - 응답 데이터 상태 : " + "\n"
					+ " code=[" + iamportPaymentResponse.getCode() + "]" + "\n"
					+ " status=[" + iamportPayment.getStatus() + "]" + "\n"
					+ " message=[" + iamportPaymentResponse.getMessage() + "]" + "\n"
					+ " failReason=[" + iamportPayment.getFailReason() + "]";
			throw failPayment(
				payment,
				member,
				"portOne 응답 데이터와 DB에 저장된 merchantUid가 일치하지 않습니다." + "\n"
					+ "[PortOne 응답 값 : " + iamportMerchantUid + "\n"
					+ " | DB 값 : " + payment.getMerchantUid() + " ]",
				pgFailMessage,
				ErrorCode.MERCHANT_UID_MISMATCH
			);
		}

		String iamportPaymentMethodTypeStr = iamportPayment.getPayMethod();
		Optional<PaymentMethodType> paymentMethodType = PaymentMethodType.from(iamportPaymentMethodTypeStr);
		if (paymentMethodType.isEmpty()) {

			String pgFailMessage =
				"PortOne 결제 검증 실패 - 응답 데이터 상태 : " + "\n"
					+ " code=[" + iamportPaymentResponse.getCode() + "]" + "\n"
					+ " status=[" + iamportPayment.getStatus() + "]" + "\n"
					+ " message=[" + iamportPaymentResponse.getMessage() + "]" + "\n"
					+ " failReason=[" + iamportPayment.getFailReason() + "]";
			throw failPayment(
				payment,
				member,
				"지원하지 않거나, 올바르지 않은 결제 수단입니다. "
					+ "[PortOne 응답 값 : " + iamportPaymentMethodTypeStr + " ]",
				pgFailMessage,
				ErrorCode.INVALID_PAYMENT_METHODTYPE
			);
		}

		PaymentMethodType iamportPaymentMethodType = paymentMethodType.get();
		if (!payment.getPaymentMethodType().equals(iamportPaymentMethodType)) {

			String pgFailMessage =
				"PortOne 결제 검증 실패 - 응답 데이터 상태 : " + "\n"
					+ " code=[" + iamportPaymentResponse.getCode() + "]" + "\n"
					+ " status=[" + iamportPayment.getStatus() + "]" + "\n"
					+ " message=[" + iamportPaymentResponse.getMessage() + "]" + "\n"
					+ " failReason=[" + iamportPayment.getFailReason() + "]";
			throw failPayment(
				payment,
				member,
				"portOne 응답 데이터와 DB에 저장된 결제 수단이 일치하지 않습니다. " + "\n"
					+ "[PortOne 응답 값 : " + iamportPaymentMethodType + "\n"
					+ " | DB 값 : " + payment.getPaymentMethodType() + " ]",
				pgFailMessage,
				ErrorCode.PAYMENT_METHOD_MISMATCH
			);
		}

		Optional<PaymentProvider> paymentProvider = PaymentProvider.from(iamportPayment.getPgProvider());
		if (paymentProvider.isEmpty()) {

			String pgFailMessage =
				"PortOne 결제 검증 실패 - 응답 데이터 상태 : " + "\n"
					+ " code=[" + iamportPaymentResponse.getCode() + "]" + "\n"
					+ " status=[" + iamportPayment.getStatus() + "]" + "\n"
					+ " message=[" + iamportPaymentResponse.getMessage() + "]" + "\n"
					+ " failReason=[" + iamportPayment.getFailReason() + "]";
			throw failPayment(
				payment,
				member,
				"지원하지 않거나, 올바르지 않은 결제 대행사입니다. "
					+ "[PortOne 응답 값 : " + iamportPayment.getPgProvider() + " ]",
				pgFailMessage,
				ErrorCode.INVALID_PAYMENT_PROVIDER
			);
		}

		PaymentProvider iamportPaymentProvider = paymentProvider.get();
		if (!payment.getPaymentProvider().equals(iamportPaymentProvider)) {

			String pgFailMessage =
				"PortOne 결제 검증 실패 - 응답 데이터 상태 : " + "\n"
					+ " code=[" + iamportPaymentResponse.getCode() + "]" + "\n"
					+ " status=[" + iamportPayment.getStatus() + "]" + "\n"
					+ " message=[" + iamportPaymentResponse.getMessage() + "]" + "\n"
					+ " provider=[" + iamportPayment.getPgProvider() + "]" + "\n"
					+ " failReason=[" + iamportPayment.getFailReason() + "]";
			throw failPayment(
				payment,
				member,
				"portOne 응답 데이터와 DB에 저장된 결제 대행사가 일치하지 않습니다." + "\n"
					+ "[PortOne 응답 값 : " + iamportPaymentProvider + "\n"
					+ " | DB 값 : " + payment.getPaymentProvider() + " ]",
				pgFailMessage,
				ErrorCode.PAYMENT_PROVIDER_MISMATCH
			);
		}

		LocalDateTime paidAt = null;
		if (iamportPayment.getPaidAt() != null) {
			paidAt = LocalDateTime.ofInstant(
				iamportPayment.getPaidAt().toInstant(),
				ZoneId.of("Asia/Seoul")
			);
		}

		String cardSnapshot = cardSnapshotLabel(iamportPayment, requestDto.getPaymentCard());

		payment.updateAfterPaymentVerification(
			iamportPgTid,
			iamportImpUid,
			requestDto.getPaymentCard(),
			iamportPaymentMethodType,
			paidAt,
			cardSnapshot
		);

		reservation.changeToAwaitingApproval();

		PaymentHistory paymentHistory = PaymentHistory.createPaymentSuccessPaymentHistory(
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
	private String cardSnapshotLabel(com.siot.IamportRestClient.response.Payment iamportPayment, String cardName) {

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

	/**
	 * 결제 환불 메서드
	 * - impUid 유무에 따라 분기처리
	 * - TODO : 로컬 크리에이터 거절시 결제 히스토리 userId 고민하기
	 */
	@Transactional
	public PaymentResponseDto refundPayment(Long paymentId, PaymentRefundRequestDto requestDto) {

		Payment payment = paymentRepository.findByIdOrElseThrow(paymentId);
		Reservation reservation = payment.getReservation();
		Member member = reservation.getMember();

		PaymentStatus paymentStatus = payment.getPaymentStatus();
		Set<PaymentStatus> refundablePaymentStatuses = PaymentStatus.refundablePaymentStatusSet();
		if (!refundablePaymentStatuses.contains(paymentStatus)) {
			throw new CustomException(ErrorCode.PAYMENT_NOT_REFUNDABLE);
		}

		ReservationStatus reservationStatus = reservation.getReservationStatus();
		Set<ReservationStatus> refundableReservationStatuses = ReservationStatus.refundableReservationStatusSet();
		if (!refundableReservationStatuses.contains(reservationStatus)) {
			throw new CustomException(ErrorCode.RESERVATION_NOT_REFUNDABLE);
		}

		String refundReason = requestDto.getRefundReason();
		String impUid = payment.getImpUid();

		if (impUid == null || impUid.isBlank()) {

			BigDecimal refundableAmount = calculateRefundableAmount(payment);
			if (refundableAmount.compareTo(BigDecimal.ZERO) <= 0) {
				throw failPaymentRefund(
					payment,
					member,
					" 환불 금액이 정상적이지 않습니다." + "\n"
						+ "[DB 환불 금액 값 : " + payment.getPaymentCost() + "\n"
						+ " | 계산된 환불 금액 값 : " + refundableAmount + " ]",
					null,
					ErrorCode.PAYMENT_NOT_REFUNDABLE
				);
			}

			String fallbackReason = "impUid 값을 받을 수 없어 merchantUid 기준으로 잔여 금액 환불 처리";
			return refundByMerchantUid(member, payment, refundableAmount, fallbackReason);
		}
		if (refundReason == null || refundReason.isBlank()) {
			throw new CustomException(ErrorCode.PAYMENT_REFUND_REASON_REQUIRED);
		}

		return refundByImpUid(member, payment, reservation, refundReason);
	}

	/**
	 * merchantUid 사용한 환불 메서드
	 * - 장애 등 impUid 못 받는 이슈 발생 시, merchantUid로 환불 진행 -> fallback 용도
	 * - 관리자 환불 진행시 사용
	 */
	private PaymentResponseDto refundByMerchantUid(Member member, Payment payment, BigDecimal refundAmount, String refundReason) {

		String merchantUid = payment.getMerchantUid();
		if (merchantUid == null || merchantUid.isBlank()) {
			throw new CustomException(ErrorCode.MERCHANT_UID_NOT_FOUND);
		}

		CancelData cancelData = new CancelData(merchantUid, false, refundAmount);
		cancelData.setChecksum(refundAmount);
		cancelData.setReason(refundReason);

		IamportResponse<com.siot.IamportRestClient.response.Payment> iamportPaymentResponse;
		try {
			iamportPaymentResponse = iamportClient.cancelPaymentByImpUid(cancelData);
		} catch (IamportResponseException | IOException exception) {

			String pgFailMessage =
				"PortOne 결제 환불 실패 - IamportResponseException 발생";
			throw failPaymentRefund(
				payment,
				member,
				"merchantUid 값으로 portOne 결제 취소 호출 실패" + "\n"
					+ "[DB merchantUid 값 : " + merchantUid + " ]",
				pgFailMessage,
				ErrorCode.MERCHANT_UID_NOT_FOUND_BY_IAMPORT
			);
		}

		com.siot.IamportRestClient.response.Payment iamportPayment = iamportPaymentResponse.getResponse();

		if (iamportPayment == null) {

			String pgFailMessage =
				"PortOne 결제 환불 실패 : " + "\n"
					+ " code=[" + iamportPaymentResponse.getCode() + "]" + "\n"
					+ " message=[" + iamportPaymentResponse.getMessage() + "]";
			throw failPaymentRefund(
				payment,
				member,
				"portOne 응답 데이터가 null 입니다.",
				pgFailMessage,
				ErrorCode.PAYMENT_REFUND_FAILED
			);
		}

		if (!"cancelled".equalsIgnoreCase(iamportPayment.getStatus())) {

			String pgFailMessage =
				"PortOne 결제 환불 실패 - 응답 데이터 상태 : " + "\n"
					+ " code=[" + iamportPaymentResponse.getCode() + "]" + "\n"
					+ " status=[" + iamportPayment.getStatus() + "]" + "\n"
					+ " message=[" + iamportPaymentResponse.getMessage() + "]" + "\n"
					+ " amount=[" + iamportPayment.getAmount() + "]" + "\n"
					+ " cancelAmount=[" + iamportPayment.getCancelAmount() + "]";
			throw failPaymentRefund(
				payment,
				member,
				"portOne 응답 데이터가 정상적인 환불 완료(cancelled) 상태가 아닙니다.",
				pgFailMessage,
				ErrorCode.PAYMENT_REFUND_FAILED
			);
		}

		BigDecimal iamportCancelAmount = iamportPayment.getCancelAmount();
		BigDecimal previousRefundAmount;
		if (payment.getTotalRefundAmount() != null) {
			previousRefundAmount = payment.getTotalRefundAmount();
		} else {
			previousRefundAmount = BigDecimal.ZERO;
		}
		BigDecimal accumulatedRefundAmount = previousRefundAmount.add(refundAmount);

		if (iamportCancelAmount == null) {

			String pgFailMessage =
				"PortOne 결제 환불 실패 : " + "\n"
					+ " code=[" + iamportPaymentResponse.getCode() + "]" + "\n"
					+ " status=[" + iamportPayment.getStatus() + "]" + "\n"
					+ " message=[" + iamportPaymentResponse.getMessage() + "]" + "\n"
					+ " cancelAmount=[" + iamportCancelAmount + "]";
			paymentHistoryService.saveRefundFailureHistory(
				payment,
				member,
				"portOne 응답 데이터에 환불 금액이 없습니다.(null 값)" + "\n"
					+ "[PortOne 누적 환불 금액 값 : " + iamportCancelAmount + "\n"
					+ " | DB 누적 환불 금액 값 : " + accumulatedRefundAmount + " ]",
				pgFailMessage
			);
		} else if (iamportCancelAmount.compareTo(accumulatedRefundAmount) != 0) {

			String pgFailMessage =
				"PortOne 결제 환불 실패 : " + "\n"
					+ " code=[" + iamportPaymentResponse.getCode() + "]" + "\n"
					+ " status=[" + iamportPayment.getStatus() + "]" + "\n"
					+ " message=[" + iamportPaymentResponse.getMessage() + "]";
			paymentHistoryService.saveRefundFailureHistory(
				payment,
				member,
				"portOne 누적 환불 금액과 DB 누적 환불 금액이 일치하지 않습니다." + "\n"
					+ "[portOne 누적 환불 금액 값 : " + iamportCancelAmount + "\n"
					+ " | DB 누적 환불 금액 값 : " + accumulatedRefundAmount + " ]",
				pgFailMessage
			);
		}

		LocalDateTime refundedAt = null;
		if (iamportPayment.getCancelledAt() != null) {
			refundedAt = LocalDateTime.ofInstant(
				iamportPayment.getCancelledAt().toInstant(),
				ZoneId.of("Asia/Seoul")
			);
		}

		payment.refundSuccess(
			refundedAt,
			refundAmount,
			refundReason
		);

		PaymentHistory paymentHistory = PaymentHistory.createPaymentRefundSuccessPaymentHistory(
			payment,
			member
		);
		paymentHistoryRepository.save(paymentHistory);

		return PaymentResponseDto.from(payment);
	}

	/**
	 * impUid 사용한 환불 메서드
	 * - impUid로 환불 정책에 따라 환불 진행
	 * - 스케줄 남은 일수에 따라서 부분 환불 가능
	 * - 로컬 크리에이터가 거절시 전액 환불
	 */
	private PaymentResponseDto refundByImpUid(Member member, Payment payment, Reservation reservation, String refundReason) {

		String impUid = payment.getImpUid();

		if (refundReason == null || refundReason.isBlank()) {
			throw new CustomException(ErrorCode.PAYMENT_REFUND_REASON_REQUIRED);
		}

		ReservationStatus reservationStatus = reservation.getReservationStatus();
		BigDecimal paymentCost = payment.getPaymentCost();
		BigDecimal refundAmount;

		if (reservationStatus == ReservationStatus.REJECTED) {
			refundAmount = paymentCost;
		} else {
			ProgramSchedule programSchedule = reservation.getProgramSchedule();
			LocalDate scheduleDate = programSchedule.getScheduleDate();
			LocalDate today = LocalDate.now();

			int daysLeft = (int) ChronoUnit.DAYS.between(today, scheduleDate);

			BigDecimal refundRate = calculateRefundRate(daysLeft);
			if (refundRate == null) {
				throw new CustomException(ErrorCode.PAYMENT_NOT_REFUNDABLE);
			}

			refundAmount = paymentCost
				.multiply(refundRate)
				.setScale(0, RoundingMode.HALF_UP);
			if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
				throw failPaymentRefund(
					payment,
					member,
					" 환불 비율 계산에 따른 환불 금액이 정상적이지 않습니다." + "\n"
						+ "[DB 환불 금액 값 : " + paymentCost + "\n"
						+ " | 계산된 환불 금액 값 : " + refundAmount + " ]",
					null,
					ErrorCode.PAYMENT_NOT_REFUNDABLE
				);
			}

		}

		CancelData cancelData = new CancelData(impUid, true, refundAmount);
		cancelData.setChecksum(paymentCost);
		cancelData.setReason(refundReason);

		IamportResponse<com.siot.IamportRestClient.response.Payment> iamportPaymentResponse;
		try {
			iamportPaymentResponse = iamportClient.cancelPaymentByImpUid(cancelData);
		} catch (IamportResponseException | IOException exception) {

			String pgFailMessage =
				"PortOne 결제 환불 실패 - IamportResponseException 발생";
			throw failPaymentRefund(
				payment,
				member,
				"impUid 값으로 portOne 결제 취소 호출 실패" + "\n"
					+ "[DB impUid 값 : " + impUid + " ]",
				pgFailMessage,
				ErrorCode.IMP_UID_NOT_FOUND_BY_IAMPORT
			);
		}

		com.siot.IamportRestClient.response.Payment iamportPayment = iamportPaymentResponse.getResponse();

		if (iamportPayment == null) {

			String pgFailMessage =
				"PortOne 결제 환불 실패 : " + "\n"
					+ " code=[" + iamportPaymentResponse.getCode() + "]" + "\n"
					+ " message=[" + iamportPaymentResponse.getMessage() + "]";
			throw failPaymentRefund(
				payment,
				member,
				"portOne 응답 데이터가 null 입니다.",
				pgFailMessage,
				ErrorCode.PAYMENT_REFUND_FAILED
			);
		}

		if (!"cancelled".equalsIgnoreCase(iamportPayment.getStatus())) {

			String pgFailMessage =
				"PortOne 결제 환불 실패 - 응답 데이터 상태 : " + "\n"
					+ " code=[" + iamportPaymentResponse.getCode() + "]" + "\n"
					+ " status=[" + iamportPayment.getStatus() + "]" + "\n"
					+ " message=[" + iamportPaymentResponse.getMessage() + "]" + "\n"
					+ " amount=[" + iamportPayment.getAmount() + "]" + "\n"
					+ " cancelAmount=[" + iamportPayment.getCancelAmount() + "]";
			throw failPaymentRefund(
				payment,
				member,
				"portOne 응답 데이터가 정상적인 환불 완료(cancelled) 상태가 아닙니다.",
				pgFailMessage,
				ErrorCode.PAYMENT_REFUND_FAILED
			);
		}

		BigDecimal iamportCancelAmount = iamportPayment.getCancelAmount();
		if (iamportCancelAmount == null) {

			String pgFailMessage =
				"PortOne 결제 환불 실패 : " + "\n"
					+ " code=[" + iamportPaymentResponse.getCode() + "]" + "\n"
					+ " status=[" + iamportPayment.getStatus() + "]" + "\n"
					+ " message=[" + iamportPaymentResponse.getMessage() + "]" + "\n"
					+ " cancelAmount=[" + iamportCancelAmount + "]";
			paymentHistoryService.saveRefundFailureHistory(
				payment,
				member,
				"portOne 응답 데이터에 환불 금액이 없습니다.(null 값)" + "\n"
					+ "[PortOne 환불 금액 값 : " + iamportCancelAmount + "\n"
					+ " | DB 환불 금액 값 : " + refundAmount + " ]",
				pgFailMessage

			);
		} else if (iamportCancelAmount.compareTo(refundAmount) != 0) {

			String pgFailMessage =
				"PortOne 결제 환불 실패 : " + "\n"
					+ " code=[" + iamportPaymentResponse.getCode() + "]" + "\n"
					+ " status=[" + iamportPayment.getStatus() + "]" + "\n"
					+ " message=[" + iamportPaymentResponse.getMessage() + "]";
			paymentHistoryService.saveRefundFailureHistory(
				payment,
				member,
				"portOne 환불 금액과 DB 환불 금액이 일치하지 않습니다." + "\n"
					+ "[portOne 환불 금액 값 : " + iamportCancelAmount + "\n"
					+ " | DB 환불 금액 값 : " + refundAmount + " ]",
				pgFailMessage
			);
		}

		LocalDateTime refundedAt = null;
		if (iamportPayment.getCancelledAt() != null) {
			refundedAt = LocalDateTime.ofInstant(
				iamportPayment.getCancelledAt().toInstant(),
				ZoneId.of("Asia/Seoul")
			);
		}

		payment.refundSuccess(
			refundedAt,
			refundAmount,
			refundReason
		);

		PaymentHistory paymentHistory = PaymentHistory.createPaymentRefundSuccessPaymentHistory(
			payment,
			member
		);
		paymentHistoryRepository.save(paymentHistory);

		return PaymentResponseDto.from(payment);
	}

	/**
	 * 프로그램 스케줄 기준 남은 날짜로 환불 비율 계산
	 */
	private BigDecimal calculateRefundRate(int daysLeft) {

		if (daysLeft >= 6) {
			return BigDecimal.valueOf(1.00);
		}

		return switch (daysLeft) {
			case 5 -> BigDecimal.valueOf(0.90);
			case 4 -> BigDecimal.valueOf(0.80);
			case 3 -> BigDecimal.valueOf(0.70);
			case 2 -> BigDecimal.valueOf(0.60);
			default -> null;
		};
	}

	/**
	 * 환불 실패 처리 메서드
	 */
	private CustomException failPaymentRefund(Payment payment, Member member, String refundFailedReason, String pgFailMessage,
		ErrorCode errorCode) {

		paymentHistoryService.saveRefundFailureHistory(payment, member, refundFailedReason, pgFailMessage);
		return new CustomException(errorCode);
	}

	/**
	 * 관리자용 결제 환불 메서드
	 * - 일반 환불 정책과 별도로 환불 처리 가능 (예약 & 결제 상태 체크 안함 - 상태 검증은 추후 고민 후 리팩토링)
	 * - 이상 케이스나 운영상 문제로 환불 필요시 사용
	 */
	@Transactional
	public PaymentResponseDto refundPaymentForAdmin(Long memberId, Long paymentId, PaymentRefundRequestDto requestDto) {

		Member member = memberRepository.findActiveByIdOrThrow(memberId);

		if (member.getMemberRole() != MemberRole.ADMIN) {
			throw new CustomException(ErrorCode.PAYMENT_REFUND_NOT_ALLOWED);
		}

		Payment payment = paymentRepository.findByIdOrElseThrow(paymentId);

		String refundReason = requestDto.getRefundReason();
		if (refundReason == null || refundReason.isBlank()) {
			throw new CustomException(ErrorCode.PAYMENT_REFUND_REASON_REQUIRED);
		}

		BigDecimal refundableAmount = calculateRefundableAmount(payment);
		if (refundableAmount.compareTo(BigDecimal.ZERO) <= 0) {
			throw failPaymentRefund(
				payment,
				member,
				" 환불 금액이 정상적이지 않습니다." + "\n"
					+ "[DB 환불 금액 값 : " + payment.getPaymentCost() + "\n"
					+ " | 계산된 환불 금액 값 : " + refundableAmount + " ]",
				null,
				ErrorCode.PAYMENT_NOT_REFUNDABLE
			);
		}

		refundByMerchantUid(member, payment, refundableAmount, refundReason);
		Reservation reservation = reservationRepository.findByIdOrElseThrow(payment.getReservation().getId());
		reservation.cancelReservation(refundReason);

		return PaymentResponseDto.from(payment);
	}

	/**
	 * 환불 가능한 금액 계산 메서드
	 */
	private BigDecimal calculateRefundableAmount(Payment payment) {

		BigDecimal paymentCost = payment.getPaymentCost();
		BigDecimal totalRefundAmount;
		if (payment.getTotalRefundAmount() != null) {
			totalRefundAmount = payment.getTotalRefundAmount();
		} else {
			totalRefundAmount = BigDecimal.ZERO;
		}

		return paymentCost.subtract(totalRefundAmount);
	}

}
