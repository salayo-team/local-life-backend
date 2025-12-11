package com.salayo.locallifebackend.domain.review.dto;

import com.salayo.locallifebackend.domain.review.enums.ReviewSortType;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class ReviewSearchRequestDto {

	public static final int DEFAULT_SIZE = 10;

	private final List<Long> tagIds;
	private final List<Long> aptitudeIds;
	private final List<Long> regionIds;
	private final Long programGroupId;
	private final String keyword;
	private final ReviewSortType sort;
	private final Integer page;
	private final Integer size;

	@Builder
	public ReviewSearchRequestDto(List<Long> tagIds, List<Long> aptitudeIds,
		List<Long> regionIds, Long programGroupId, String keyword,
		ReviewSortType sort, Integer page, Integer size) {
		this.tagIds = tagIds;
		this.aptitudeIds = aptitudeIds;
		this.regionIds = regionIds;
		this.programGroupId = programGroupId;
		this.keyword = keyword;
		this.sort = sort;
		this.page = page;
		this.size = size;
	}

	public int getPage() {
		return (page == null || page < 0) ? 0 : page;
	}

	public int getSize() {
		return (size == null || size <= 0) ? DEFAULT_SIZE : size;
	}
}
