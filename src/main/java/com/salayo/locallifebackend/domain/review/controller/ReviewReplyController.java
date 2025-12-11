package com.salayo.locallifebackend.domain.review.controller;

import com.salayo.locallifebackend.domain.review.dto.ReviewReplyRequestDto;
import com.salayo.locallifebackend.domain.review.dto.ReviewReplyResponseDto;
import com.salayo.locallifebackend.domain.review.service.ReviewReplyService;
import com.salayo.locallifebackend.global.dto.CommonResponseDto;
import com.salayo.locallifebackend.global.security.MemberDetails;
import com.salayo.locallifebackend.global.success.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "ReviewReply", description = "리뷰 답글 API (로컬 크리에이터용)")
public class ReviewReplyController {

	private final ReviewReplyService reviewReplyService;

	public ReviewReplyController(ReviewReplyService reviewReplyService) {
		this.reviewReplyService = reviewReplyService;
	}

	@PostMapping("/reviews/{reviewId}/replies")
	@PreAuthorize("hasRole('LOCAL_CREATOR')")
	@Operation(
		summary = "리뷰 답글 작성",
		description = "로컬 크리에이터가 자신의 프로그램에 달린 리뷰에 대해 1개의 답글을 작성합니다."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "201",
			description = "리뷰 답글 작성 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = CommonResponseDto.class)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "잘못된 요청 (이미 답글이 존재하는 리뷰)"
		),
		@ApiResponse(
			responseCode = "401",
			description = "인증되지 않은 사용자"
		),
		@ApiResponse(
			responseCode = "403",
			description = "해당 프로그램의 로컬 크리에이터가 아닌 경우"
		),
		@ApiResponse(
			responseCode = "404",
			description = "리뷰를 찾을 수 없음"
		)
	})
	public CommonResponseDto<ReviewReplyResponseDto> createReviewReply(
		@PathVariable Long reviewId,
		@Valid @RequestBody ReviewReplyRequestDto requestDto,
		@AuthenticationPrincipal MemberDetails memberDetails) {

		ReviewReplyResponseDto responseDto = reviewReplyService.createReviewReply(
			reviewId,
			requestDto,
			memberDetails.getMember()
		);

		return CommonResponseDto.success(SuccessCode.CREATE_SUCCESS, responseDto);
	}
}
