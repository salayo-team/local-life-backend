package com.salayo.locallifebackend.domain.program.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "program_schedule_time")
public class ProgramScheduleTime {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; //체험 프로그램 스케줄 시간 고유 식별자

	@Column(name = "program_id", nullable = false)
	private Long programId; //체험 프로그램 고유 식별자 fk

	@Column(name = "schedule_count", nullable = false)
	private Integer scheduleCount; //하루 스케줄 회차

	@Column(name = "schedule_duration", nullable = false)
	private Integer scheduleDuration; //스케줄 소요 시간

	@Column(name = "start_time", nullable = false)
	private LocalTime startTime; //스케줄 시작시간

	@Column(name = "end_time", nullable = true)
	private LocalTime endTime; //스케줄 종료 시간

	@Builder
	public ProgramScheduleTime(Long programId, Integer scheduleCount, Integer scheduleDuration, LocalTime startTime, LocalTime endTime) {
		this.programId = programId;
		this.scheduleCount = scheduleCount;
		this.scheduleDuration = scheduleDuration;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public void connectToProgram(Long programId) {
		this.programId = programId;
	}

}
