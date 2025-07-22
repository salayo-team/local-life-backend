package com.salayo.locallifebackend.domain.ai.aptitude.dto;

import lombok.Getter;

@Getter
public class CanRetakeTestResponseDto {

	private final boolean canRetake;
	private final int currentTestCount;
	private final int maxTestCount;

	public CanRetakeTestResponseDto(boolean canRetake, int currentTestCount, int maxTestCount) {
		this.canRetake = canRetake;
		this.currentTestCount = currentTestCount;
		this.maxTestCount = maxTestCount;
	}
}
