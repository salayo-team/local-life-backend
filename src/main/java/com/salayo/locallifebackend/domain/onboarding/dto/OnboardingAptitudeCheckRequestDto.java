package com.salayo.locallifebackend.domain.onboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "적성 인지 여부 확인 요청 DTO")
public record OnboardingAptitudeCheckRequestDto(

	@Schema(description = "사용자가 자신의 적성을 알고 있는지 여부",
		example = "true", required = true)
	@NotNull(message = "적성 인지 여부를 선택해주세요")
	Boolean knowsAptitude
) {

}
