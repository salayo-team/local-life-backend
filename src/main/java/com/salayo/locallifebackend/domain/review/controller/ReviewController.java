package com.salayo.locallifebackend.domain.review.controller;

import com.salayo.locallifebackend.domain.review.dto.ReviewRequestDto;
import com.salayo.locallifebackend.domain.review.dto.ReviewResponseDto;
import com.salayo.locallifebackend.domain.review.service.ReviewService;
import com.salayo.locallifebackend.global.dto.CommonResponseDto;
import com.salayo.locallifebackend.global.dto.PaginationResponseDto;
import com.salayo.locallifebackend.global.security.MemberDetails;
import com.salayo.locallifebackend.global.success.SuccessCode;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
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
		@Valid @RequestBody ReviewRequestDto requestDto,
		@AuthenticationPrincipal MemberDetails memberDetails) {

		ReviewResponseDto responseDto = reviewService.createReview(
			requestDto,
			memberDetails.getMember(),
			programId,
			reservationId
		);

		return CommonResponseDto.success(SuccessCode.CREATE_SUCCESS, responseDto);
	}

	@GetMapping("/my/reviews")
	@PreAuthorize("hasRole('USER')")
	public CommonResponseDto<PaginationResponseDto<ReviewResponseDto>> getMyReviews(
		@AuthenticationPrincipal MemberDetails memberDetails,
		@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		PaginationResponseDto<ReviewResponseDto> responsePage = reviewService.getMyReviews(
			memberDetails.getMember(), pageable);

		return CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, responsePage);
	}

	@GetMapping("/programs/{programId}/reviews")
	public CommonResponseDto<PaginationResponseDto<ReviewResponseDto>> getProgramReviews(
		@PathVariable Long programId,
		@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

		PaginationResponseDto<ReviewResponseDto> responsePage = reviewService.getProgramReviews(programId, pageable);

		return CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, responsePage);
	}

	@GetMapping("/admin/reviews")
	@PreAuthorize("hasRole('ADMIN')")
	public CommonResponseDto<PaginationResponseDto<ReviewResponseDto>> getAllReviews(
		@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

		PaginationResponseDto<ReviewResponseDto> responsePage = reviewService.getAllReviews(pageable);

		return CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, responsePage);
	}

	@PutMapping("/reviews/{reviewId}")
	@PreAuthorize("hasRole('USER')")
	public CommonResponseDto<ReviewResponseDto> updateReview(
		@PathVariable Long reviewId,
		@Valid @RequestBody ReviewRequestDto requestDto,
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

		return CommonResponseDto.success(SuccessCode.DELETE_SUCCESS, null);
	}
}
