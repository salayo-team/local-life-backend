package com.salayo.locallifebackend.domain.ai.aptitude.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "적성 검사 답변 요청 DTO")
public class AptitudeAnswerRequestDto {

	@Schema(description = "현재 단계 번호", example = "1", required = true, minimum = "1", maximum = "5")
	@NotNull(message = "단계는 필수입니다.")
	private Integer step;
	
	@Schema(description = "질문 내용", example = "너가 제일 좋아하는 활동은 뭐야?", required = true)
	@NotBlank(message = "질문 내용은 필수입니다.")
	private String questionText;
	
	@Schema(description = "사용자 답변", example = "나는 자연 속에서 산책하는 것을 좋아해. 특히 숲길을 걸으면서 새소리를 듣는 게 좋아.", required = true, maxLength = 500)
	@NotBlank(message = "답변은 필수입니다.")
	private String answer;
}
