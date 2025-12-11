package com.salayo.locallifebackend.domain.review.dto;

import com.salayo.locallifebackend.domain.review.entity.ReviewReply;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
@Schema(description = "리뷰 답글 응답 DTO")
public class ReviewReplyResponseDto {

	@Schema(description = "답글 ID", example = "1")
	private final Long replyId;

	@Schema(description = "로컬 크리에이터 닉네임", example = "로컬카페사장")
	private final String creatorNickname;

	@Schema(description = "답글 내용", example = "방문해주셔서 감사합니다! 다음에도 또 놀러 오세요 :)")
	private final String content;

	@Schema(description = "답글 작성 일시", example = "2025-12-11T10:15:30")
	private final LocalDateTime createdAt;

	public ReviewReplyResponseDto(ReviewReply reviewReply) {
		this.replyId = reviewReply.getId();
		this.creatorNickname = reviewReply.getMember().getNickname();
		this.content = reviewReply.getContent();
		this.createdAt = reviewReply.getCreatedAt();
	}
}
