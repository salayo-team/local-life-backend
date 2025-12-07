package com.salayo.locallifebackend.domain.ai.aptitude.dto;

import com.salayo.locallifebackend.domain.ai.aptitude.enums.AptitudeType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "사용자 적성 정보 응답 DTO")
public class UserAptitudeResponseDto {

	@Schema(
		description = "사용자의 최종 적성 타입",
		example = "NATURE"
	)
	private final AptitudeType aptitudeType;

	@Schema(
		description = "적성 타입 한글명",
		example = "자연친화"
	)
	private final String title;

	@Schema(
		description = "적성 타입에 대한 설명",
		example = "자연 속에서의 삶, 생태적 가치와 조화롭게 살아가는 것을 추구하는 유형입니다."
	)
	private final String description;

	@Builder
	public UserAptitudeResponseDto(AptitudeType aptitudeType) {
		this.aptitudeType = aptitudeType;
		this.title = aptitudeType.getTitle();
		this.description = aptitudeType.getDescription();
	}
}
