package com.salayo.locallifebackend.domain.program.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.salayo.locallifebackend.domain.program.enums.DayName;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ProgramCreateRequestDto {

	@NotNull(message = "적성 카테고리는 필수값입니다.")
	private Long aptitudeCategoryId; //적성 카테고리 고유 식별자

	@NotNull(message = "지역 카테고리는 필수값입니다.")
	private Long regionCategoryId; //지역 카테고리 고유 식별자

	@NotBlank(message = "체험 프로그램 제목은 필수값입니다.")
	private String title; //프로그램 제목

	@NotBlank(message = "체험 프로그램 설명은 필수값입니다.")
	private String description; //프로그램 설명

	@NotBlank(message = "체험 프로그램 커리큘럼 설명은 필수값입니다.")
	private String curriculumDescription; //커리큘럼 설명

	@NotBlank(message = "체험 프로그램 위치는 필수값입니다.")
	private String location; //체험 위치

	@NotNull(message = "체험 프로그램 가격은 필수값입니다.")
	@DecimalMin(value = "1.0", message = "체험 프로그램 가격 {value}원 이상이여야 합니다.")
	@DecimalMax(value = "5000000.0", message = "체험 프로그램 가격은 {value}원 이하여야 합니다.")
	private BigDecimal price; //체험 가격

	private BigDecimal percent; //체험 할인율

	@NotNull(message = "최대 정원은 필수값입니다.")
	@Min(value = 1, message = "최대 정원은 {value}명 이상이어야 합니다.")
	@Max(value = 100, message = "최대 정원은 {value}명 이하여야 합니다.")
	private Integer maxCapacity;

	@NotNull(message = "최소 정원은 필수값입니다.")
	@Min(value = 1, message = "최소 정원은 {value}명 이상이어야 합니다.")
	@Max(value = 100, message = "최소 정원은 {value}명 이하여야 합니다.")
	private Integer minCapacity;

	@NotNull(message = "체험 프로그램 시작일은 필수값입니다.")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate startDate; //체험 시작일

	@NotNull(message = "체험 프로그램 종료일은 필수값입니다.")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate endDate; //체험 종료일

	@NotEmpty(message = "최소 한 개 이상의 요일이 필요합니다.")
	private List<DayName> programDays;

	@NotEmpty(message = "최소 한 개 이상의 스케줄 시간이 필요합니다.")
	private List<ProgramScheduleTimeRequestDto> programScheduleTimes;

}
