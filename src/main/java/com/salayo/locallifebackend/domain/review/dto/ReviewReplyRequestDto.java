package com.salayo.locallifebackend.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "리뷰 답글 작성 요청 DTO")
public class ReviewReplyRequestDto {

	@Schema(
		description = "답글 내용",
		example = "방문해주셔서 감사합니다! 다음에도 또 놀러 오세요 :)",
		required = true,
		maxLength = 500
	)
	@NotBlank(message = "답글 내용은 필수입니다.")
	@Size(max = 500, message = "답글은 최대 500자까지 작성 가능합니다.")
	private String content;
}

