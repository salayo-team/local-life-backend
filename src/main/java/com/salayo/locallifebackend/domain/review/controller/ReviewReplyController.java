package com.salayo.locallifebackend.domain.review.controller;

import com.salayo.locallifebackend.domain.review.dto.ReviewReplyRequestDto;
import com.salayo.locallifebackend.domain.review.dto.ReviewReplyResponseDto;
import com.salayo.locallifebackend.domain.review.service.ReviewReplyService;
import com.salayo.locallifebackend.global.dto.CommonResponseDto;
import com.salayo.locallifebackend.global.security.MemberDetails;
import com.salayo.locallifebackend.global.success.SuccessCode;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReviewReplyController {

	private final ReviewReplyService reviewReplyService;

	public ReviewReplyController(ReviewReplyService reviewReplyService) {
		this.reviewReplyService = reviewReplyService;
	}

	@PostMapping("/reviews/{reviewId}/replies")
	@PreAuthorize("hasRole('LOCAL_CREATOR')")
	public CommonResponseDto<ReviewReplyResponseDto> createReviewReply(
		@PathVariable Long reviewId,
		@RequestBody ReviewReplyRequestDto requestDto,
		@AuthenticationPrincipal MemberDetails memberDetails) {

		ReviewReplyResponseDto responseDto = reviewReplyService.createReviewReply(
			reviewId,
			requestDto,
			memberDetails.getMember()
		);

		return CommonResponseDto.success(SuccessCode.CREATE_SUCCESS, responseDto);
	}
}
