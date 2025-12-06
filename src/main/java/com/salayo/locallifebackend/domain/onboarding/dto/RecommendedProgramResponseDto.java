package com.salayo.locallifebackend.domain.onboarding.dto;

import com.salayo.locallifebackend.domain.program.enums.LocalSpecialized;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 추천 프로그램 응답 DTO 온보딩 완료 후 사용자 맞춤 프로그램 정보
 */
@Builder
public record RecommendedProgramResponseDto(
	Long programId,
	String title,
	String businessName,
	String description,
	String location,
	BigDecimal price,
	BigDecimal finalPrice,
	BigDecimal percent,
	Integer maxCapacity,
	Integer minCapacity,
	LocalDate startDate,
	LocalDate endDate,
	String aptitudeCategory,
	String regionCategory,
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
