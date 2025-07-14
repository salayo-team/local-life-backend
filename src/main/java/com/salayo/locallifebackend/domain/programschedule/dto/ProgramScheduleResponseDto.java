package com.salayo.locallifebackend.domain.programschedule.dto;

import com.salayo.locallifebackend.domain.programschedule.enums.ProgramScheduleStatus;
import com.salayo.locallifebackend.global.enums.DeletedStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ProgramScheduleResponseDto {

	private Long id; //체험 프로그램 스케줄 고유 식별자

	private Long programId; //체험 프로그램 고유 식별자

	private LocalDate scheduleDate; //스케줄 날짜

	private LocalTime startTime; //스케줄 시작시간

	private LocalTime endTime; //스케줄 종료시간

	private ProgramScheduleStatus programScheduleStatus; //스케줄 운영상태

	private LocalDateTime createdAt; //스케줄 생성일

	private LocalDateTime modifiedAt; //스케줄 수정일

	private DeletedStatus deletedStatus; //스케줄 삭제 상태
}
