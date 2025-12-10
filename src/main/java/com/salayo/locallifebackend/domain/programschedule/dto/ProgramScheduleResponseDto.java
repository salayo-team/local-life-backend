package com.salayo.locallifebackend.domain.programschedule.dto;

import com.salayo.locallifebackend.domain.programschedule.enums.ProgramScheduleStatus;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ProgramScheduleResponseDto {

	private final Long id; //체험 프로그램 스케줄 고유 식별자

	private final LocalDate scheduleDate; //스케줄 날짜

	private final ProgramScheduleStatus programScheduleStatus; //스케줄 운영상태

}
