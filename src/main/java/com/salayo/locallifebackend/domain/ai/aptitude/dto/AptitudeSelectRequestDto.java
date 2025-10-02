package com.salayo.locallifebackend.domain.ai.aptitude.dto;

import com.salayo.locallifebackend.domain.ai.aptitude.enums.AptitudeType;
import jakarta.validation.constraints.NotNull;

public record AptitudeSelectRequestDto(

	@NotNull(message = "적성 타입은 필수입니다.")
	AptitudeType aptitudeType
	) {}
