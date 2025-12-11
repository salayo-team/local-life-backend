package com.salayo.locallifebackend.domain.review.dto;

import com.salayo.locallifebackend.domain.review.entity.ReviewTag;
import lombok.Getter;

@Getter
public class ReviewTagResponseDto {

	private final Long tagId;
	private final String name;

	public ReviewTagResponseDto(ReviewTag reviewTag) {
		this.tagId = reviewTag.getId();
		this.name = reviewTag.getName();
	}
}
