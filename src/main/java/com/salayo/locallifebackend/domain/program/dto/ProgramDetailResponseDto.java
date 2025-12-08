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
public class ProgramDetailResponseDto {

	private final Long id;

	private final Long providerId;

	private final Long aptitudeCategoryId;

	private final Long regionCategoryId;

	private final Long originalProgramId;

	private final Long programGroupId;

	private final String businessName;

	private final String title;

	private final String description;

	private final String curriculumDescription;

	private final String location;

	private final BigDecimal price;

	private final BigDecimal percent;

	private final BigDecimal finalPrice;

	private final Integer maxCapacity;

	private final Integer minCapacity;

	private final LocalDate startDate;

	private final LocalDate endDate;

	private final Integer count;

	private final LocalSpecialized isLocalSpecialized;

	private final ProgramStatus programStatus;

	private final Integer reviewCount;

	private final BigDecimal averageRating;

	private final String thumbnailUrl;

	private final List<String> detailImageUrls;

	private final LocalDateTime createdAt;

	private final List<ProgramDayResponseDto> programDays;

	private final List<ProgramScheduleResponseDto> programSchedules;

	private final List<ProgramScheduleTimeResponseDto> programScheduleTimes;

	public static ProgramDetailResponseDto from(Program program, String thumbnailUrl, List<String> detailImageUrls) {

		Long originalProgramId = null;
		if(program.getOriginalProgram() != null){
			originalProgramId = program.getOriginalProgram().getId();
		}

		Long programGroupId = null;
		if(program.getProgramGroup() != null){
			programGroupId = program.getProgramGroup().getId();
		}

		return ProgramDetailResponseDto.builder()
			.id(program.getId())
			.providerId(program.getMember().getId())
			.aptitudeCategoryId(program.getAptitudeCategory().getId())
			.regionCategoryId(program.getRegionCategory().getId())
			.originalProgramId(originalProgramId)
			.programGroupId(programGroupId)
			.businessName(program.getBusinessName())
			.title(program.getTitle())
			.description(program.getDescription())
			.curriculumDescription(program.getCurriculumDescription())
			.location(program.getLocation())
			.price(program.getPrice())
			.percent(program.getPercent())
			.finalPrice(program.getFinalPrice())
			.maxCapacity(program.getMaxCapacity())
			.minCapacity(program.getMinCapacity())
			.startDate(program.getStartDate())
			.endDate(program.getEndDate())
			.count(program.getCount())
			.isLocalSpecialized(program.getIsLocalSpecialized())
			.programStatus(program.getProgramStatus())
			.reviewCount(program.getReviewCount())
			.averageRating(program.getAverageRating())
			.thumbnailUrl(thumbnailUrl)
			.detailImageUrls(detailImageUrls)
			.createdAt(program.getCreatedAt())
			.programDays(program.getProgramDays().stream()
				.map(day -> ProgramDayResponseDto.builder()
					.id(day.getId())
					.dayName(day.getDayName())
					.build())
				.toList())
			.programSchedules(program.getProgramSchedules().stream()
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
