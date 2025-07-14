package com.salayo.locallifebackend.domain.program.dto;

import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ProgramScheduleTimeResponseDto {

	private Integer scheduleCount; //스케줄 회차

	private Integer scheduleDuration; //스케줄 소요 시간

	private LocalTime startTime; //스케줄 시작시간

	private LocalTime endTime; //스케줄 종료시간
}
