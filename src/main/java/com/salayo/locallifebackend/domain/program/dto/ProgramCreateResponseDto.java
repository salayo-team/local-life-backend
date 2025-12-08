package com.salayo.locallifebackend.domain.program.dto;


import com.salayo.locallifebackend.domain.program.entity.Program;
import com.salayo.locallifebackend.domain.program.enums.LocalSpecialized;
import com.salayo.locallifebackend.domain.program.enums.ProgramStatus;
import com.salayo.locallifebackend.domain.programschedule.dto.ProgramScheduleResponseDto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProgramCreateResponseDto {

	private final Long id; //체험 프로그램 고유 식별자

	private final Long providerId; //프로그램 제공자(로컬 크리에이터) 고유 식별자

	private final Long aptitudeCategoryId; //적성 카테고리 고유 식별자

	private final Long regionCategoryId; //지역 카테고리 고유 식별자

	private final String businessName; //상호명

	private final String title; //프로그램 제목

	private final String description; //프로그램 설명

	private final String curriculumDescription; //프로그램 커리큘럼 설명

	private final String location; //체험 위치

	private final BigDecimal price; //체험 가격

	private final BigDecimal percent; //체험 할인률

	private final BigDecimal finalPrice; //최종 가격

	private final Integer maxCapacity; //스케줄 최대 정원

	private final Integer minCapacity; //스케줄 최소 정원

	private final LocalDate startDate; //체험 프로그램 시작일

	private final LocalDate endDate; //체험 프로그램 종료일

	private final Integer count; //조회수

	private final LocalSpecialized isLocalSpecialized; //지역특화 여부

	private final ProgramStatus programStatus; //프로그램 운영상태

	private final LocalDateTime createdAt; //프로그램 생성일

	private final List<ProgramDayResponseDto> programDays; //운영 요일 리스트

	private final List<ProgramScheduleResponseDto> programSchedule; // 프로그램 스케줄

	private final List<ProgramScheduleTimeResponseDto> programScheduleTimes; //스케줄 회차별 시간 리스트

	public static ProgramCreateResponseDto from(Program program) {

		return ProgramCreateResponseDto.builder()
			.id(program.getId())
			.providerId(program.getMember().getId())
			.aptitudeCategoryId(program.getAptitudeCategory().getId())
			.regionCategoryId(program.getRegionCategory().getId())
			.businessName(program.getBusinessName())
			.title(program.getTitle())
			.description(program.getDescription())
			.curriculumDescription(program.getCurriculumDescription())
			.location(program.getLocation())
			.price(program.getPrice())
			.percent(program.getPercent())
			.finalPrice(program.getFinalPrice())
			.minCapacity(program.getMinCapacity())
			.maxCapacity(program.getMaxCapacity())
			.startDate(program.getStartDate())
			.endDate(program.getEndDate())
			.count(program.getCount())
			.isLocalSpecialized(program.getIsLocalSpecialized())
			.programStatus(program.getProgramStatus())
			.createdAt(program.getCreatedAt())
			.programDays(program.getProgramDays().stream()
				.map(day -> ProgramDayResponseDto.builder()
					.id(day.getId())
					.dayName(day.getDayName())
					.build())
				.toList())
			.programSchedule(program.getProgramSchedules().stream()
				.map(schedule -> ProgramScheduleResponseDto.builder()
					.id(schedule.getId())
					.scheduleDate(schedule.getScheduleDate())
					.programScheduleStatus(schedule.getProgramScheduleStatus())
					.build())
				.toList())
			.programScheduleTimes(program.getProgramScheduleTimes().stream()
				.map(scheduleTime -> ProgramScheduleTimeResponseDto.builder()
					.id(scheduleTime.getId())
					.scheduleCount(scheduleTime.getScheduleCount())
					.scheduleDuration(scheduleTime.getScheduleDuration())
					.startTime(scheduleTime.getStartTime())
					.endTime(scheduleTime.getEndTime())
					.build())
				.toList())
			.build();
	}

}
