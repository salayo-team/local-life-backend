package com.salayo.locallifebackend.domain.review.dto;

import com.salayo.locallifebackend.domain.review.entity.Review;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ReviewResponseDto {

	private final Long reviewId;
	private final String memberNickname;
	private final String programTitle;
	private final LocalDate experienceEndDate;
	private final String content;
	private final boolean isModified;
	private final LocalDateTime createdAt;
	private final int viewCount;
	private final int likeCount;
	private final List<String> likeTags;
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
			.map(mapping -> mapping.getReviewTag().getName())
			.collect(Collectors.toList());
		this.replies = review.getReplies().stream()
			.map(ReviewReplyResponseDto::new)
			.collect(Collectors.toList());
	}

}
