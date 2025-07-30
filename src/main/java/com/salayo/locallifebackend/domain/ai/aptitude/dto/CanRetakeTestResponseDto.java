package com.salayo.locallifebackend.domain.ai.aptitude.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "재검사 가능 여부 응답 DTO")
public class CanRetakeTestResponseDto {

	@Schema(description = "재검사 가능 여부", example = "true")
	private final boolean canRetake;
	
	@Schema(description = "현재까지 검사한 횟수", example = "2", minimum = "0", maximum = "5")
	private final int currentTestCount;
	
	@Schema(description = "최대 검사 가능 횟수", example = "5")
	private final int maxTestCount;

	public CanRetakeTestResponseDto(boolean canRetake, int currentTestCount, int maxTestCount) {
		this.canRetake = canRetake;
		this.currentTestCount = currentTestCount;
		this.maxTestCount = maxTestCount;
	}
}
