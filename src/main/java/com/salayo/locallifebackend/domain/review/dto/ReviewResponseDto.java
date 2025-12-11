package com.salayo.locallifebackend.domain.review.dto;

import com.salayo.locallifebackend.domain.review.entity.Review;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "리뷰 상세/목록 응답 DTO")
public class ReviewResponseDto {

	@Schema(description = "리뷰 ID", example = "10")
	private final Long reviewId;

	@Schema(description = "리뷰 작성자 닉네임", example = "local_life_user")
	private final String memberNickname;

	@Schema(description = "체험 프로그램 제목", example = "양양 로컬 브루어리 투어")
	private final String programTitle;

	@Schema(description = "체험 종료 일자", example = "2026-08-15", format = "date")
	private final LocalDate experienceEndDate;

	@Schema(description = "리뷰 내용", example = "양양에 대한 애정이 느껴지는 멋진 체험이었어요.")
	private final String content;

	@Schema(description = "리뷰 수정 여부", example = "true")
	private final boolean isModified;

	@Schema(description = "리뷰 작성 일시", example = "2026-02-16T14:23:10", format = "date-time")
	private final LocalDateTime createdAt;

	@Schema(description = "조회 수", example = "42")
	private final int viewCount;

	@Schema(description = "좋아요 수", example = "7")
	private final int likeCount;

	@Schema(
		description = "리뷰에 연결된 태그 이름 목록",
		example = "[\"호스트가 친절해요\", \"로컬이 잘 드러나요\"]"
	)
	private final List<String> likeTags;

	@Schema(description = "리뷰에 달린 답글 목록")
	private final List<ReviewReplyResponseDto> replies;

	@Builder
	public ReviewResponseDto(Review review) {
		this.reviewId = review.getId();
		this.memberNickname = review.getMember().getNickname();
		this.programTitle = review.getProgram().getTitle();
		this.experienceEndDate = review.getProgram().getEndDate();
		this.content = review.getContent();
		this.isModified = review.getModifiedAt().isAfter(review.getCreatedAt());
		this.createdAt = review.getCreatedAt();
		this.viewCount = review.getViewCount();
		this.likeCount = review.getLikeCount();
		this.likeTags = review.getTagMappings().stream()
			.map(mapping -> mapping.getReviewTag().getTagName())
			.collect(Collectors.toList());
		this.replies = review.getReplies().stream()
			.map(ReviewReplyResponseDto::new)
			.collect(Collectors.toList());
	}

}
