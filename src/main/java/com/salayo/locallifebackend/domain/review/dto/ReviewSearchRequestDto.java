package com.salayo.locallifebackend.domain.review.dto;

import com.salayo.locallifebackend.domain.review.enums.ReviewSortType;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReviewSearchRequestDto {

	public static final int DEFAULT_SIZE = 10;

	private List<Long> tagIds;
	private List<Long> aptitudeIds;
	private List<Long> regionIds;
	private Long programGroupId;
	private String keyword;
	private ReviewSortType sort;
	private Integer page;
	private Integer size;

	public int getPage() {
		return (page == null || page < 0) ? 0 : page;
	}

	public int getSize() {
		return (size == null || size <= 0) ? DEFAULT_SIZE : size;
	}
}
