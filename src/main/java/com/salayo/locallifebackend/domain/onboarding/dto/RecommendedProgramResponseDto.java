package com.salayo.locallifebackend.domain.onboarding.dto;

import com.salayo.locallifebackend.domain.program.enums.LocalSpecialized;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Schema(description = "추천 프로그램 정보 응답 DTO")
public record RecommendedProgramResponseDto(

	@Schema(description = "프로그램 ID", example = "101")
	Long programId,

	@Schema(description = "프로그램 제목", example = "마을 숲 힐링 산책 프로그램")
	String title,

	@Schema(description = "운영 사업자명", example = "로컬리프 협동조합")
	String businessName,

	@Schema(description = "프로그램 상세 설명",
		example = "자연 속에서 음식을 만들고 함께 나누는 힐링 프로그램")
	String description,

	@Schema(description = "프로그램 위치", example = "제주특별자치도 제주시 애월읍")
	String location,

	@Schema(description = "정가", example = "30000")
	BigDecimal price,

	@Schema(description = "할인가 (없으면 정가 표시)", example = "24000")
	BigDecimal finalPrice,

	@Schema(description = "최대 정원", example = "20")
	Integer maxCapacity,

	@Schema(description = "할인 비율", example = "20.0")
	BigDecimal percent,

	@Schema(description = "최소 정원", example = "5")
	Integer minCapacity,

	@Schema(description = "프로그램 시작일", example = "2026-01-01")
	LocalDate startDate,

	@Schema(description = "프로그램 종료일", example = "2026-06-30")
	LocalDate endDate,

	@Schema(description = "적성 카테고리", example = "NATURE")
	String aptitudeCategory,

	@Schema(description = "지역 카테고리", example = "URBAN")
	String regionCategory,

	@Schema(description = "로컬 특화 여부", example = "YES")
	LocalSpecialized isLocalSpecialized
) {

	/**
	 * 할인 여부 확인
	 */
	public boolean hasDiscount() {
		return percent != null && percent.compareTo(BigDecimal.ZERO) > 0;
	}

	/**
	 * 프로그램 진행 가능 여부 확인
	 */
	public boolean isActive() {
		LocalDate now = LocalDate.now();
		return !now.isBefore(startDate) && !now.isAfter(endDate);
	}
}
