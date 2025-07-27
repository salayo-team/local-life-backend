package com.salayo.locallifebackend.domain.review.controller;

import com.salayo.locallifebackend.domain.review.dto.ReviewRequestDto;
import com.salayo.locallifebackend.domain.review.dto.ReviewResponseDto;
import com.salayo.locallifebackend.domain.review.service.ReviewService;
import com.salayo.locallifebackend.global.dto.CommonResponseDto;
import com.salayo.locallifebackend.global.security.MemberDetails;
import com.salayo.locallifebackend.global.success.SuccessCode;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ReviewController {

	private final ReviewService reviewService;

	public ReviewController(ReviewService reviewService) {
		this.reviewService = reviewService;
	}

	@PostMapping("/programs/{programId}/reviews")
	@PreAuthorize("hasRole('USER')")
	public CommonResponseDto<ReviewResponseDto> createReview(
		@PathVariable Long programId,
		@RequestParam Long reservationId,
		@RequestBody ReviewRequestDto requestDto,
		@AuthenticationPrincipal MemberDetails memberDetails) {

		ReviewResponseDto responseDto = reviewService.createReview(
			requestDto,
			memberDetails.getMember(),
			programId,
			reservationId
		);

		return CommonResponseDto.success(SuccessCode.CREATE_SUCCESS, responseDto);
	}

	@GetMapping("/mypage/reviews")
	@PreAuthorize("hasRole('USER')")
	public CommonResponseDto<List<ReviewResponseDto>> getMyReviews(
		@AuthenticationPrincipal MemberDetails memberDetails) {

		List<ReviewResponseDto> reviews = reviewService.getMyReviews(
			memberDetails.getMember()
		);

		return CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, reviews);
	}

	@GetMapping("/programs/{programId}/reviews")
	public CommonResponseDto<List<ReviewResponseDto>> getProgramReviews(
		@PathVariable Long programId) {

		List<ReviewResponseDto> reviews = reviewService.getProgramReviews(programId);

		return CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, reviews);
	}

	@GetMapping("/admin/reviews")
	@PreAuthorize("hasRole('ADMIN')")
	public CommonResponseDto<List<ReviewResponseDto>> getAllReviews() {

		List<ReviewResponseDto> reviews = reviewService.getAllReviews();

		return CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, reviews);
	}

	@PutMapping("/reviews/{reviewId}")
	@PreAuthorize("hasRole('USER')")
	public CommonResponseDto<ReviewResponseDto> updateReview(
		@PathVariable Long reviewId,
		@RequestBody ReviewRequestDto requestDto,
		@AuthenticationPrincipal MemberDetails memberDetails) {

		ReviewResponseDto responseDto = reviewService.updateReview(
			reviewId,
			requestDto,
			memberDetails.getMember()
		);

		return CommonResponseDto.success(SuccessCode.UPDATE_SUCCESS, responseDto);
	}

	@DeleteMapping("/reviews/{reviewId}")
	@PreAuthorize("hasRole('USER')")
	public CommonResponseDto<String> deleteReview(
		@PathVariable Long reviewId,
		@AuthenticationPrincipal MemberDetails memberDetails) {

		reviewService.deleteReview(reviewId, memberDetails.getMember());

		return CommonResponseDto.success(SuccessCode.DELETE_SUCCESS, "리뷰가 삭제되었습니다.");
	}
}
