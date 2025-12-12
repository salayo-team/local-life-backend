package com.salayo.locallifebackend.domain.review.service;

import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.review.dto.ReviewReplyRequestDto;
import com.salayo.locallifebackend.domain.review.dto.ReviewReplyResponseDto;
import com.salayo.locallifebackend.domain.review.entity.Review;
import com.salayo.locallifebackend.domain.review.entity.ReviewReply;
import com.salayo.locallifebackend.global.enums.DeletedStatus;
import com.salayo.locallifebackend.domain.review.repository.ReviewReplyRepository;
import com.salayo.locallifebackend.domain.review.repository.ReviewRepository;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import com.salayo.locallifebackend.global.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ReviewReplyService {

	private final ReviewRepository reviewRepository;
	private final ReviewReplyRepository reviewReplyRepository;
	private final ReviewCacheService reviewCacheService;


	public ReviewReplyService(ReviewRepository reviewRepository, ReviewReplyRepository reviewReplyRepository,
		ReviewCacheService reviewCacheService) {
		this.reviewRepository = reviewRepository;
		this.reviewReplyRepository = reviewReplyRepository;
		this.reviewCacheService = reviewCacheService;
	}

	@Transactional
	public ReviewReplyResponseDto createReviewReply(Long reviewId, ReviewReplyRequestDto requestDto, Member creator) {
		log.info("리뷰 답글 작성 시작 - reviewId: {}, creatorId: {}", reviewId, creator.getId());

		Review review = reviewRepository.findByIdAndDeletedStatusOrThrow(reviewId, DeletedStatus.DISPLAYED);

		validateProgramOwnership(review, creator);

		if (reviewReplyRepository.existsByReviewAndDeletedStatus(review, DeletedStatus.DISPLAYED)) {
			throw new CustomException(ErrorCode.DUPLICATE_REVIEW_REPLY);
		}

		ReviewReply reviewReply = ReviewReply.builder()
			.review(review)
			.member(creator)
			.content(requestDto.getContent())
			.build();

		ReviewReply savedReply = reviewReplyRepository.save(reviewReply);

		reviewCacheService.invalidateReviewCacheByPattern(review.getProgram().getId());

		return new ReviewReplyResponseDto(savedReply);
	}

	private void validateProgramOwnership(Review review, Member creator) {
		if (!review.getProgram().getMember().getId().equals(creator.getId())) {
			throw new CustomException(ErrorCode.FORBIDDEN_ACCESS);
		}
	}
}
