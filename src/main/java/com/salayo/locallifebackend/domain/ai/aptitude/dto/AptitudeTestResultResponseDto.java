package com.salayo.locallifebackend.domain.ai.aptitude.dto;

import com.salayo.locallifebackend.domain.ai.aptitude.enums.AptitudeType;
import lombok.Getter;

@Getter
public class AptitudeTestResultResponseDto {

	private final AptitudeType aptitudeType;
	private final String title;
	private final String description;

	public AptitudeTestResultResponseDto(AptitudeType aptitudeType) {
		this.aptitudeType = aptitudeType;
		this.title = aptitudeType.getTitle();
		this.description = aptitudeType.getDescription();
	}
}
