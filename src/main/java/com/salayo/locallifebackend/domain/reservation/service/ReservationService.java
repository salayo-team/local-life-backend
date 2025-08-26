package com.salayo.locallifebackend.domain.reservation.service;

import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.member.enums.MemberRole;
import com.salayo.locallifebackend.domain.member.repository.MemberRepository;
import com.salayo.locallifebackend.domain.programschedule.entity.ProgramSchedule;
import com.salayo.locallifebackend.domain.programschedule.repository.ProgramScheduleRepository;
import com.salayo.locallifebackend.domain.reservation.dto.ReservationCreateRequestDto;
import com.salayo.locallifebackend.domain.reservation.entity.Reservation;
import com.salayo.locallifebackend.domain.reservation.enums.ReservationStatus;
import com.salayo.locallifebackend.domain.reservation.repository.ReservationRepository;
import com.salayo.locallifebackend.global.enums.DeletedStatus;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
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
	 */
	@Transactional
	public Reservation createReservation(Long memberId, ReservationCreateRequestDto requestDto){

		Member member = memberRepository.findByIdOrElseThrow(memberId);
		if(member.getMemberRole().equals(MemberRole.LOCAL_CREATOR)){
			throw new CustomException(ErrorCode.RESERVATION_NOT_ALLOWED);
		}

		ProgramSchedule programSchedule = programScheduleRepository.findByIdOrElseThrow(requestDto.getProgramScheduleId());

		Reservation reservation = Reservation.builder()
			.member(member)
			.programSchedule(programSchedule)
			.reservationStatus(ReservationStatus.REQUESTED)
			.deletedStatus(DeletedStatus.DISPLAYED)
			.build();

		reservationRepository.save(reservation);

		return reservation;
	}

}

