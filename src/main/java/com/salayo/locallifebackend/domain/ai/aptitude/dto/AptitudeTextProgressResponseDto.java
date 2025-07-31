package com.salayo.locallifebackend.domain.ai.aptitude.dto;

import com.salayo.locallifebackend.domain.ai.aptitude.enums.AptitudeType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "적성 검사 진행 상태 응답 DTO")
public class AptitudeTextProgressResponseDto {

	@Schema(description = "현재 진행 단계", example = "2", minimum = "1", maximum = "5")
	private final Integer step;
	
	@Schema(description = "전체 단계 수", example = "5")
	private final Integer totalStep;
	
	@Schema(description = "다음 질문 정보 (검사 완료 시 null)")
	private final AptitudeQuestionResponseDto aptitudeQuestionResponseDto;
	
	@Schema(description = "검사 완료 여부", example = "false")
	private final Boolean isComplete;
	
	@Schema(description = "최종 적성 결과 (검사 진행 중일 때는 null)")
	private final AptitudeType finalAptitude;

	@Builder
	public AptitudeTextProgressResponseDto(Integer step, Integer totalStep, 
			AptitudeQuestionResponseDto aptitudeQuestionResponseDto, Boolean isComplete, 
			AptitudeType finalAptitude) {
		this.step = step;
		this.totalStep = totalStep;
		this.aptitudeQuestionResponseDto = aptitudeQuestionResponseDto;
		this.isComplete = isComplete;
		this.finalAptitude = finalAptitude;
	}
}
