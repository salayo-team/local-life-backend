package com.salayo.locallifebackend.domain.review.dto;

import com.salayo.locallifebackend.domain.review.entity.ReviewReply;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class ReviewReplyResponseDto {

	private final Long replyId;
	private final String creatorNickname;
	private final String content;
	private final LocalDateTime createdAt;

	public ReviewReplyResponseDto(ReviewReply reviewReply) {
		this.replyId = reviewReply.getId();
		this.creatorNickname = reviewReply.getMember().getNickname();
		this.content = reviewReply.getContent();
		this.createdAt = reviewReply.getCreatedAt();
	}
}
