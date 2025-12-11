package com.salayo.locallifebackend.domain.review.controller;

import com.salayo.locallifebackend.domain.review.dto.ReviewRequestDto;
import com.salayo.locallifebackend.domain.review.dto.ReviewResponseDto;
import com.salayo.locallifebackend.domain.review.dto.ReviewSearchRequestDto;
import com.salayo.locallifebackend.domain.review.dto.ReviewTagResponseDto;
import com.salayo.locallifebackend.domain.review.service.ReviewService;
import com.salayo.locallifebackend.global.dto.CommonResponseDto;
import com.salayo.locallifebackend.global.dto.PaginationResponseDto;
import com.salayo.locallifebackend.global.security.MemberDetails;
import com.salayo.locallifebackend.global.success.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Review", description = "리뷰 관련 API")
public class ReviewController {

	private final ReviewService reviewService;

	public ReviewController(ReviewService reviewService) {
		this.reviewService = reviewService;
	}

	@PostMapping("/programs/{programId}/reviews")
	@PreAuthorize("hasRole('USER')")
	@Operation(
		summary = "리뷰 작성",
		description = "체험을 완료한 프로그램에 대해 리뷰를 작성합니다. 동일 프로그램에는 한 번만 리뷰를 작성할 수 있습니다."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "201",
			description = "리뷰 작성 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = CommonResponseDto.class)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "잘못된 요청 (별점 누락 또는 범위/단위 오류, 리뷰 작성 불가 상태, 이미 리뷰가 존재하는 경우, 잘못된 프로그램/예약 매핑 등)"
		),
		@ApiResponse(
			responseCode = "401",
			description = "인증되지 않은 사용자"
		),
		@ApiResponse(
			responseCode = "403",
			description = "자신의 예약이 아니거나 리뷰 작성 권한이 없는 경우"
		),
		@ApiResponse(
			responseCode = "404",
			description = "프로그램 또는 예약을 찾을 수 없음"
		)
	})
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
	@Operation(
		summary = "나의 리뷰 목록 조회",
		description = "현재 로그인한 사용자가 작성한 리뷰 목록을 페이지네이션 형태로 조회합니다."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "나의 리뷰 목록 조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = CommonResponseDto.class)
			)
		),
		@ApiResponse(
			responseCode = "401",
			description = "인증되지 않은 사용자"
		)
	})
	public CommonResponseDto<PaginationResponseDto<ReviewResponseDto>> getMyReviews(
		@AuthenticationPrincipal MemberDetails memberDetails,
		@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		PaginationResponseDto<ReviewResponseDto> responsePage = reviewService.getMyReviews(
			memberDetails.getMember(), pageable);

		return CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, responsePage);
	}

	@GetMapping("/programs/{programId}/reviews")
	@Operation(
		summary = "프로그램 리뷰 목록 조회",
		description = "특정 체험 프로그램에 작성된 리뷰 목록을 페이지네이션 형태로 조회합니다."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "프로그램 리뷰 목록 조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = CommonResponseDto.class)
			)
		),
		@ApiResponse(
			responseCode = "404",
			description = "프로그램을 찾을 수 없음"
		)
	})
	public CommonResponseDto<PaginationResponseDto<ReviewResponseDto>> getProgramReviews(
		@PathVariable Long programId,
		@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

		PaginationResponseDto<ReviewResponseDto> responsePage = reviewService.getProgramReviews(programId, pageable);

		return CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, responsePage);
	}

	@GetMapping("/reviews/{reviewId}")
	@Operation(
		summary = "리뷰 상세 조회",
		description = "리뷰 ID로 단일 리뷰 상세 정보를 조회합니다. 조회 시 조회 수가 증가합니다."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "리뷰 상세 조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = CommonResponseDto.class)
			)
		),
		@ApiResponse(
			responseCode = "404",
			description = "리뷰를 찾을 수 없음"
		)
	})
	public CommonResponseDto<ReviewResponseDto> getReviewDetail(@PathVariable Long reviewId) {

		ReviewResponseDto responseDto = reviewService.getReviewDetail(reviewId);

		return CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, responseDto);
	}

	@PostMapping("/reviews/{reviewId}/like")
	@PreAuthorize("hasRole('USER')")
	@Operation(
		summary = "리뷰 좋아요",
		description = "특정 리뷰에 좋아요를 누릅니다. 이미 좋아요를 눌렀다면 오류가 발생합니다."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "201",
			description = "리뷰 좋아요 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = CommonResponseDto.class)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "이미 좋아요를 누른 리뷰인 경우 또는 잘못된 요청"
		),
		@ApiResponse(
			responseCode = "401",
			description = "인증되지 않은 사용자"
		),
		@ApiResponse(
			responseCode = "404",
			description = "리뷰를 찾을 수 없음"
		)
	})
	public CommonResponseDto<Void> likeReview(
		@PathVariable Long reviewId,
		@AuthenticationPrincipal MemberDetails memberDetails) {

		reviewService.likeReview(reviewId, memberDetails.getMember());

		return CommonResponseDto.success(SuccessCode.CREATE_SUCCESS, null);
	}

	@DeleteMapping("/reviews/{reviewId}/like")
	@PreAuthorize("hasRole('USER')")
	@Operation(
		summary = "리뷰 좋아요 취소",
		description = "특정 리뷰에 눌렀던 좋아요를 취소합니다."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "리뷰 좋아요 취소 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = CommonResponseDto.class)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "좋아요를 누르지 않은 리뷰인 경우 또는 잘못된 요청"
		),
		@ApiResponse(
			responseCode = "401",
			description = "인증되지 않은 사용자"
		),
		@ApiResponse(
			responseCode = "404",
			description = "리뷰를 찾을 수 없음"
		)
	})
	public CommonResponseDto<Void> unlikeReview(
		@PathVariable Long reviewId,
		@AuthenticationPrincipal MemberDetails memberDetails) {

		reviewService.unlikeReview(reviewId, memberDetails.getMember());

		return CommonResponseDto.success(SuccessCode.DELETE_SUCCESS, null);
	}

	@GetMapping("/reviews/tags")
	@Operation(
		summary = "리뷰 태그 전체 조회",
		description = "리뷰 작성 시 선택할 수 있는 활성화된 리뷰 태그 목록을 조회합니다."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "리뷰 태그 조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = CommonResponseDto.class)
			)
		)
	})
	public CommonResponseDto<List<ReviewTagResponseDto>> getAllTags() {

		List<ReviewTagResponseDto> tags = reviewService.getAllTags();

		return CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, tags);
	}

	@GetMapping("/reviews/search")
	@Operation(
		summary = "리뷰 검색 및 필터링",
		description = "태그, 키워드, 적성 카테고리, 지역 카테고리, 프로그램 그룹, 정렬 조건을 조합하여 리뷰를 검색합니다."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "리뷰 검색 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = CommonResponseDto.class)
			)
		)
	})
	public CommonResponseDto<PaginationResponseDto<ReviewResponseDto>> searchReviews(
		@ModelAttribute ReviewSearchRequestDto requestDto) {

		PaginationResponseDto<ReviewResponseDto> responsePage = reviewService.searchReviews(requestDto);

		return CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, responsePage);
	}

	@GetMapping("/admin/reviews")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
		summary = "전체 리뷰 목록 조회(관리자용)",
		description = "관리자가 전체 리뷰 목록을 페이지네이션 형태로 조회합니다."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "전체 리뷰 조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = CommonResponseDto.class)
			)
		),
		@ApiResponse(
			responseCode = "401",
			description = "인증되지 않은 사용자"
		),
		@ApiResponse(
			responseCode = "403",
			description = "관리자 권한이 없는 경우"
		)
	})
	public CommonResponseDto<PaginationResponseDto<ReviewResponseDto>> getAllReviews(
		@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

		PaginationResponseDto<ReviewResponseDto> responsePage = reviewService.getAllReviews(pageable);

		return CommonResponseDto.success(SuccessCode.FETCH_SUCCESS, responsePage);
	}

	@PutMapping("/reviews/{reviewId}")
	@PreAuthorize("hasRole('USER')")
	@Operation(
		summary = "리뷰 수정",
		description = "본인이 작성한 리뷰를 수정합니다. 이미 답글이 달린 리뷰는 수정할 수 없습니다."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "리뷰 수정 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = CommonResponseDto.class)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "잘못된 요청 (별점 누락 / 1~5 사이 범위 / 0.5 단위 오류, 답글이 달린 리뷰는 수정 불가 등)"
		),
		@ApiResponse(
			responseCode = "401",
			description = "인증되지 않은 사용자"
		),
		@ApiResponse(
			responseCode = "403",
			description = "자신이 작성한 리뷰가 아닌 경우"
		),
		@ApiResponse(
			responseCode = "404",
			description = "리뷰를 찾을 수 없음"
		)
	})
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
	@Operation(
		summary = "리뷰 삭제",
		description = "본인이 작성한 리뷰를 삭제합니다. 리뷰는 소프트 삭제 처리되며, 관련 답글도 함께 삭제됩니다."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "리뷰 삭제 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = CommonResponseDto.class)
			)
		),
		@ApiResponse(
			responseCode = "401",
			description = "인증되지 않은 사용자"
		),
		@ApiResponse(
			responseCode = "403",
			description = "자신이 작성한 리뷰가 아닌 경우"
		),
		@ApiResponse(
			responseCode = "404",
			description = "리뷰를 찾을 수 없음"
		)
	})
	public CommonResponseDto<String> deleteReview(
		@PathVariable Long reviewId,
		@AuthenticationPrincipal MemberDetails memberDetails) {

		reviewService.deleteReview(reviewId, memberDetails.getMember());

		return CommonResponseDto.success(SuccessCode.DELETE_SUCCESS, null);
	}
}
