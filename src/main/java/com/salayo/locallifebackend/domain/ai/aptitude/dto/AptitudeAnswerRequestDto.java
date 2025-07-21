package com.salayo.locallifebackend.domain.ai.aptitude.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AptitudeAnswerRequestDto {

	@NotNull(message = "단계는 필수입니다.")
	private Integer step;

	@NotBlank(message = "질문 내용은 필수입니다.")
	private String questionText;

	@NotBlank(message = "답변은 필수입니다.")
	private String answer;
}
