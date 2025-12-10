package com.salayo.locallifebackend.domain.ai.aptitude.dto;

import com.salayo.locallifebackend.domain.ai.aptitude.enums.AptitudeType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record AptitudeSelectRequestDto(

	@NotNull(message = "적성 타입은 필수입니다.")
	@Schema(description = "사용자가 직접 선택하거나 변경하는 적성 타입", example = "NATURE")
	AptitudeType aptitudeType
	) {}
