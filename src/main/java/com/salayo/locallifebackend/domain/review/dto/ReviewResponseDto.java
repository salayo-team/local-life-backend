package com.salayo.locallifebackend.domain.review.dto;

import com.salayo.locallifebackend.domain.review.entity.Review;
import com.salayo.locallifebackend.domain.review.enums.ReviewStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
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
	private final List<ReviewReplyResponseDto> replies;

	public ReviewResponseDto(Review review) {
		this.reviewId = review.getId();
		this.memberNickname = review.getMember().getNickname();
		this.programTitle = review.getProgram().getTitle();
		this.experienceEndDate = review.getProgram().getEndDate();
		this.content = review.getContent();
		this.isModified = review.isModified();
		this.createdAt = review.getCreatedAt();
		this.replies = review.getReplies().stream()
			.filter(reply -> reply.getReviewStatus() == ReviewStatus.DISPLAYED)
			.map(ReviewReplyResponseDto::new)
			.collect(Collectors.toList());
	}
}
