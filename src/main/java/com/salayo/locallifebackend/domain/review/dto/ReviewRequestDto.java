package com.salayo.locallifebackend.domain.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewRequestDto {
	
	@NotBlank(message = "리뷰 내용은 필수입니다.")
	@Size(max = 500, message = "리뷰는 최대 500자까지 작성 가능합니다.")
	private String content;
}
