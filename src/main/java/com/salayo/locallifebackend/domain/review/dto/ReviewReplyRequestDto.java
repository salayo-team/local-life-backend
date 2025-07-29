package com.salayo.locallifebackend.domain.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewReplyRequestDto {
	
	@NotBlank(message = "답글 내용은 필수입니다.")
	@Size(max = 500, message = "답글은 최대 500자까지 작성 가능합니다.")
	private String content;
}
