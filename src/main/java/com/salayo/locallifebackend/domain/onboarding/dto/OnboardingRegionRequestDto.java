package com.salayo.locallifebackend.domain.onboarding.dto;

import com.salayo.locallifebackend.domain.onboarding.enums.RegionType;
import jakarta.validation.constraints.NotNull;

/**
 * 선호 지역 특징 선택 요청 DTO
 */
public record OnboardingRegionRequestDto(

	@NotNull(message = "지역 특징을 선택해주세요")
	RegionType regionType
) {

}
