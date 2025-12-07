package com.salayo.locallifebackend.domain.onboarding.dto;

import com.salayo.locallifebackend.domain.onboarding.enums.RegionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "선호 지역 특징 선택 요청 DTO")
public record OnboardingRegionRequestDto(

	@Schema(description = "선택한 지역 특징 타입 (URBAN, BALANCED, NATURE)",
		example = "URBAN", required = true)
	@NotNull(message = "지역 특징을 선택해주세요")
	RegionType regionType
) {

}
