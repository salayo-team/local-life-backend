package com.salayo.locallifebackend.domain.review.dto;

import com.salayo.locallifebackend.domain.review.enums.ReviewSortType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "리뷰 검색 조건 요청 DTO")
public class ReviewSearchRequestDto {

	public static final int DEFAULT_SIZE = 10;

	@Schema(
		description = "필터링할 리뷰 태그 ID 목록",
		example = "[1, 2, 3]"
	)
	private final List<Long> tagIds;

	@Schema(
		description = "필터링할 적성 카테고리 ID 목록",
		example = "[1, 4]"
	)
	private final List<Long> aptitudeIds;

	@Schema(
		description = "필터링할 지역 카테고리 ID 목록",
		example = "[2, 5]"
	)
	private final List<Long> regionIds;

	@Schema(
		description = "프로그램 그룹 ID (동일 그룹 내 리뷰만 조회)",
		example = "1"
	)
	private final Long programGroupId;

	@Schema(
		description = "닉네임 또는 리뷰 본문 키워드 검색",
		example = "분위기"
	)
	private final String keyword;

	@Schema(
		description = "정렬 기준",
		example = "LATEST",
		allowableValues = {
			"LATEST", "OLDEST",
			"RATING_HIGH", "RATING_LOW",
			"LIKE_COUNT", "VIEW_COUNT"
		}
	)
	private final ReviewSortType sort;

	@Schema(
		description = "페이지 번호(0부터 시작)",
		example = "0",
		minimum = "0"
	)
	private final Integer page;

	@Schema(
		description = "페이지 크기",
		example = "10",
		minimum = "1"
	)
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
