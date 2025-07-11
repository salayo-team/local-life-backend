package com.salayo.locallifebackend.domain.program.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ProgramCreateRequestDto {

	@NotNull(message = "적성 카테고리는 필수값입니다.")
	private Long aptitudeCategoryId; //적성 카테고리 고유 식별자

	@NotNull(message = "지역 카테고리는 필수값입니다.")
	private Long regionCategoryId; //지역 카테고리 고유 식별자

	@NotNull(message = "체험 프로그램 제목은 필수값입니다.")
	private String title; //프로그램 제목

	@NotNull(message = "체험 프로그램 설명은 필수값입니다.")
	private String description; //프로그램 설명

	@NotNull(message = "체험 프로그램 위치는 필수값입니다.")
	private String location; //체험 위치

	@NotNull(message = "체험 프로그램 가격은 필수값입니다.")
	@DecimalMin(value = "1.0", message = "체험 프로그램 가격 1원 이상이여야 합니다.")
	@DecimalMax(value = "5000000.0", message = "체험 프로그램 가격은 5,000,000원 이하여야 합니다.")
	private BigDecimal price; //체험 가격

	@DecimalMin(value = "1.0", message = "체험 할인율은 1 이상이어야 합니다.")
	@DecimalMax(value = "100.0", message = "체험 할인율은 100 이하이어야 합니다.")
	private BigDecimal percent; //체험 할인율

	@NotNull(message = "체험 프로그램 정원은 필수값입니다.")
	@Min(value = 1, message = "체험 프로그램 정원은 최소 1명 이상이어야 합니다.")
	@Max(value = 100, message = "체험 프로그램 정원은 최대 100명 이하여야 합니다.")
	private Integer capacity; //체험 정원

	@NotNull(message = "체험 프로그램 모집 기간은 필수값입니다.")
	private LocalDate recruitmentPeriod; //모집 기간

	@NotNull(message = "체험 프로그램 시작일은 필수값입니다.")
	private LocalDate startDate; //체험 시작일

	@NotNull(message = "체험 프로그램 종료일은 필수값입니다.")
	private LocalDate endDate; //체험 종료일

	@NotNull(message = "체험 프로그램 시작시간은 필수값입니다.")
	private LocalTime startTime; //체험 시작시간

	@NotNull(message = "체험 프로그램 종료시간은 필수값입니다.")
	private LocalTime endTime; //체험 종료시간
}
