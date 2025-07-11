package com.salayo.locallifebackend.domain.program.dto;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProgramCreateResponseDto {

	private String title; //프로그램 제목

	private String description; //프로그램 설명

	private String location; //체험 위치

	private BigDecimal price; //체험 가격

	private BigDecimal percent; //체험 할인율

	private Integer capacity; //체험 정원

	private LocalDate recruitmentPeriod; //모집 기간

	private LocalDate startDate; //체험 시작일

	private LocalDate endDate; //체험 종료일

	private LocalTime startTime; //체험 시작시간

	private LocalTime endTime; //체험 종료시간
}
