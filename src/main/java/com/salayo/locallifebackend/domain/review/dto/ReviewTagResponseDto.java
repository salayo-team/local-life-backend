package com.salayo.locallifebackend.domain.review.dto;

import com.salayo.locallifebackend.domain.review.entity.ReviewTag;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "리뷰 태그 응답 DTO")
public class ReviewTagResponseDto {

	@Schema(description = "리뷰 태그 ID", example = "1")
	private final Long tagId;

	@Schema(description = "리뷰 태그 이름", example = "로컬 크리에이터가 친절해요")
	private final String name;

	public ReviewTagResponseDto(ReviewTag reviewTag) {
		this.tagId = reviewTag.getId();
		this.name = reviewTag.getName();
	}
}
