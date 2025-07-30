package com.salayo.locallifebackend.domain.ai.aptitude.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;

@Getter
@Schema(description = "적성 검사 질문 정보 DTO")
public class AptitudeQuestionResponseDto {

	@Schema(description = "질문 순서", example = "1", minimum = "1", maximum = "5")
	private final Integer order;
	
	@Schema(description = "질문 내용", example = "너가 제일 좋아하는 활동은 뭐야?")
	private final String question;
	
	@Schema(description = "예시 답변 목록", example = "[\"텃밭을 가꾸거나 자연을 산책하는 것\", \"뭔가를 직접 만들고 꾸미는 것\", \"책이나 다큐로 지역 이야기를 탐색하는 것\", \"새로운 사람을 만나고 소통하는 것\", \"앱, 툴, 기술을 써보며 문제를 해결하는 것\"]")
	private final List<String> exampleAnswers;

	public AptitudeQuestionResponseDto(Integer order, String question, List<String> exampleAnswers) {
		this.order = order;
		this.question = question;
		this.exampleAnswers = exampleAnswers;
	}
}
