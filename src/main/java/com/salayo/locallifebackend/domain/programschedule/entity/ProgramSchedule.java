package com.salayo.locallifebackend.domain.programschedule.entity;

import com.salayo.locallifebackend.domain.program.entity.Program;
import com.salayo.locallifebackend.domain.programschedule.enums.ProgramScheduleStatus;
import com.salayo.locallifebackend.global.entity.BaseEntity;
import com.salayo.locallifebackend.global.enums.DeletedStatus;
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
public class ProgramSchedule extends BaseEntity {

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

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private DeletedStatus deletedStatus; //스케줄 삭제 여부

	@Builder
	public ProgramSchedule(Program program, LocalDate scheduleDate, LocalTime startTime, LocalTime endTime,
		ProgramScheduleStatus programScheduleStatus, DeletedStatus deletedStatus) {
		this.program = program;
		this.scheduleDate = scheduleDate;
		this.startTime = startTime;
		this.endTime = endTime;
		this.programScheduleStatus = programScheduleStatus;
		this.deletedStatus = deletedStatus;
	}
}
