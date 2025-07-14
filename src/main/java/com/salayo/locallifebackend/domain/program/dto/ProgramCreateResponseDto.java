package com.salayo.locallifebackend.domain.program.dto;


import com.salayo.locallifebackend.domain.program.enums.DayName;
import com.salayo.locallifebackend.domain.program.enums.LocalSpecialized;
import com.salayo.locallifebackend.domain.program.enums.ProgramStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProgramCreateResponseDto {

	private Long id; //체험 프로그램 고유 식별자

	private Long providerId; //프로그램 제공자(로컬 크리에이터) 고유 식별자

	private Long aptitudeCategoryId; //적성 카테고리 고유 식별자

	private Long regionCategoryId; //지역 카테고리 고유 식별자

	private String businessName; //상호명

	private String title; //프로그램 제목

	private String description; //프로그램 설명

	private String curriculumDescription; //프로그램 커리큘럼 설명

	private String location; //체험 위치

	private BigDecimal price; //체험 가격

	private BigDecimal percent; //체험 할인률

	private BigDecimal discountedPrice; //할인된 가격

	private Integer maxCapacity; //스케줄 최대 정원

	private Integer minCapacity; //스케줄 최소 정원

	private LocalDate recruitmentPeriod; //스케줄 모집 기간

	private LocalDate startDate; //체험 프로그램 시작일

	private LocalDate endDate; //체험 프로그램 종료일

	private Integer count; //조회수

	private LocalSpecialized isLocalSpecialized; //지역특화 여부

	private ProgramStatus status; //프로그램 운영상태

	private LocalDateTime createdAt; //프로그램 생성일

	private LocalDateTime modifiedAt; //프로그램 수정일

	private List<DayName> programDays; //운영 요일 리스트

	private List<ProgramScheduleTimeResponseDto> programScheduleTimes; //스케줄 회차별 시간 리스트
}
