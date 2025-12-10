package com.salayo.locallifebackend.domain.programschedule.entity;

import com.salayo.locallifebackend.domain.program.entity.Program;
import com.salayo.locallifebackend.domain.programschedule.enums.ProgramScheduleStatus;
import com.salayo.locallifebackend.global.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "program_schedule")
public class ProgramSchedule extends SoftDeletableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; //체험 프로그램 스케줄 고유 식별자

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "program_id", nullable = false)
	private Program program; //프로그램 고유 식별자

	@Column(nullable = false)
	private LocalDate scheduleDate; //스케줄 날짜

	@Column(nullable = false)
	private LocalTime startTime; //스케줄 시작시간

	@Column(nullable = false)
	private LocalTime endTime; //스케줄 종료시간

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private ProgramScheduleStatus programScheduleStatus; //스케줄 운영상태

	@Builder
	public ProgramSchedule(Program program, LocalDate scheduleDate, LocalTime startTime, LocalTime endTime,
		ProgramScheduleStatus programScheduleStatus) {
		this.program = program;
		this.scheduleDate = scheduleDate;
		this.startTime = startTime;
		this.endTime = endTime;
		this.programScheduleStatus = programScheduleStatus;
	}

	public void connectToProgram(Program program) {
		this.program = program;
	}

	/**
	 * 프로그램 삭제 상태 업데이트
	 */
	public void deleteProgramSchedule() {
		softDelete();
		this.programScheduleStatus = ProgramScheduleStatus.INACTIVE;
	}

	/**
	 * 프로그램 스케줄 생성
	 */
	public static ProgramSchedule createProgramSchedule(Program program, LocalDate scheduleDate, LocalTime startTime, LocalTime endTime) {

		return ProgramSchedule.builder()
			.program(program)
			.scheduleDate(scheduleDate)
			.startTime(startTime)
			.endTime(endTime)
			.programScheduleStatus(ProgramScheduleStatus.ACTIVE)
			.build();
	}
}
