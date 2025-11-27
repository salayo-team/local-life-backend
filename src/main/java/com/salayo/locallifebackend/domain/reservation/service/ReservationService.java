package com.salayo.locallifebackend.domain.reservation.service;

import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.member.enums.MemberRole;
import com.salayo.locallifebackend.domain.member.repository.MemberRepository;
import com.salayo.locallifebackend.domain.programschedule.entity.ProgramSchedule;
import com.salayo.locallifebackend.domain.programschedule.repository.ProgramScheduleRepository;
import com.salayo.locallifebackend.domain.reservation.dto.ReservationCreateRequestDto;
import com.salayo.locallifebackend.domain.reservation.dto.ReservationResponseDto;
import com.salayo.locallifebackend.domain.reservation.entity.Reservation;
import com.salayo.locallifebackend.domain.reservation.enums.ReservationStatus;
import com.salayo.locallifebackend.domain.reservation.repository.ReservationRepository;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationService {

	private final MemberRepository memberRepository;
	private final ProgramScheduleRepository programScheduleRepository;
	private final ReservationRepository reservationRepository;

	public ReservationService(MemberRepository memberRepository, ProgramScheduleRepository programScheduleRepository,
		ReservationRepository reservationRepository) {
		this.memberRepository = memberRepository;
		this.programScheduleRepository = programScheduleRepository;
		this.reservationRepository = reservationRepository;
	}

	/**
	 * 예약 생성 메서드
	 * - TODO : 중복 예약 동시성 제어
	 * - TODO : 예약 예외, 검증 추가
	 * - TODO : 스케줄 예약시 스케줄 정원 체크해야 함
	 * - TODO : 스케줄 최소 정원 - 정원 채워지지 않으면 전원 거절 및 자동 환불 | 스케줄 하나당 최대 받을 수 있는 정원 1명 - 100명 제한
	 */
	@Transactional
	public ReservationResponseDto createReservation(Long memberId, ReservationCreateRequestDto requestDto) {

		Member member = memberRepository.findByIdOrElseThrow(memberId);
		if (member.getMemberRole().equals(MemberRole.LOCAL_CREATOR)) {
			throw new CustomException(ErrorCode.RESERVATION_NOT_ALLOWED);
		}

		Set<ReservationStatus> inactiveReservationStatuses = ReservationStatus.inactiveReservationStatusSet();
		boolean existsActiveReservation =
			reservationRepository.existsByProgramScheduleIdAndReservationStatusNotIn(
				requestDto.getProgramScheduleId(),
				inactiveReservationStatuses
			);
		if (existsActiveReservation) {
			throw new CustomException(ErrorCode.ALREADY_RESERVATION);
		}

		ProgramSchedule programSchedule = programScheduleRepository.findByIdOrElseThrow(requestDto.getProgramScheduleId());

		Reservation reservation = Reservation.createReservation(
			member,
			programSchedule
		);
		reservationRepository.save(reservation);

		return ReservationResponseDto.from(reservation);
	}

}

