package com.salayo.locallifebackend.domain.review.service;

import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.review.dto.ReviewReplyRequestDto;
import com.salayo.locallifebackend.domain.review.dto.ReviewReplyResponseDto;
import com.salayo.locallifebackend.domain.review.entity.Review;
import com.salayo.locallifebackend.domain.review.entity.ReviewReply;
import com.salayo.locallifebackend.domain.review.enums.ReviewStatus;
import com.salayo.locallifebackend.domain.review.repository.ReviewReplyRepository;
import com.salayo.locallifebackend.domain.review.repository.ReviewRepository;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import com.salayo.locallifebackend.global.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ReviewReplyService {

	private final ReviewRepository reviewRepository;
	private final ReviewReplyRepository reviewReplyRepository;
	private final RedisTemplate<String, String> redisTemplate;

	private static final String REVIEW_CACHE_KEY = "review:program:";

	public ReviewReplyService(ReviewRepository reviewRepository, ReviewReplyRepository reviewReplyRepository,
		@Qualifier("reviewRedisTemplate") RedisTemplate<String, String> redisTemplate) {
		this.reviewRepository = reviewRepository;
		this.reviewReplyRepository = reviewReplyRepository;
		this.redisTemplate = redisTemplate;
	}

	@Transactional
	public ReviewReplyResponseDto createReviewReply(Long reviewId, ReviewReplyRequestDto requestDto, Member creator) {
		log.info("리뷰 답글 작성 시작 - reviewId: {}, creatorId: {}", reviewId, creator.getId());

		// 글자수 제한 검증 - DTO @Valid로 이미 검증됨
		// validateContentLength(requestDto.getContent());

		// 리뷰 존재 확인
		Review review = reviewRepository.findByIdAndReviewStatusOrThrow(reviewId, ReviewStatus.DISPLAYED);

		// 프로그램 소유권 확인
		validateProgramOwnership(review, creator);

		// 답글 중복 체크
		if (reviewReplyRepository.existsByReviewAndReviewStatus(review, ReviewStatus.DISPLAYED)) {
			throw new CustomException(ErrorCode.DUPLICATE_REVIEW_REPLY);
		}

		ReviewReply reviewReply = ReviewReply.builder()
			.review(review)
			.member(creator)
			.content(requestDto.getContent())
			.build();

		ReviewReply savedReply = reviewReplyRepository.save(reviewReply);

		// 캐시 무효화
		invalidateReviewCache(review.getProgram().getId());

		return new ReviewReplyResponseDto(savedReply);
	}

	private void validateProgramOwnership(Review review, Member creator) {
		// TODO: 로컬 크리에이터가 해당 프로그램의 소유자인지 확인
		// Program의 creator와 현재 사용자가 일치하는지 확인
		if (!review.getProgram().getMember().getId().equals(creator.getId())) {
			throw new CustomException(ErrorCode.FORBIDDEN_ACCESS);
		}
	}


	private void invalidateReviewCache(Long programId) {
		String cacheKey = REVIEW_CACHE_KEY + programId;
		redisTemplate.delete(cacheKey);
	}
}
