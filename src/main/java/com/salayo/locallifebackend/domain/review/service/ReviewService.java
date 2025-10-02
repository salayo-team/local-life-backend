package com.salayo.locallifebackend.domain.review.service;

import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.program.entity.Program;
import com.salayo.locallifebackend.domain.program.repository.ProgramRepository;
import com.salayo.locallifebackend.domain.reservation.entity.Reservation;
import com.salayo.locallifebackend.domain.reservation.repository.ReservationRepository;
import com.salayo.locallifebackend.domain.review.dto.ReviewRequestDto;
import com.salayo.locallifebackend.domain.review.dto.ReviewResponseDto;
import com.salayo.locallifebackend.domain.review.entity.Review;
import com.salayo.locallifebackend.global.enums.DeletedStatus;
import com.salayo.locallifebackend.domain.review.repository.ReviewReplyRepository;
import com.salayo.locallifebackend.domain.review.repository.ReviewRepository;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import com.salayo.locallifebackend.global.error.ErrorCode;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ReviewService {

	private final ReviewRepository reviewRepository;
	private final ReviewReplyRepository reviewReplyRepository;
	private final ProgramRepository programRepository;
	private final ReservationRepository reservationRepository;
	private final ReviewCacheService reviewCacheService;
	private final EntityManager entityManager;

	private static final long CACHE_TTL = 60; // 60분

	public ReviewService(ReviewRepository reviewRepository, ReviewReplyRepository reviewReplyRepository,
		ProgramRepository programRepository, ReservationRepository reservationRepository,
		ReviewCacheService reviewCacheService, EntityManager entityManager) {
		this.reviewRepository = reviewRepository;
		this.reviewReplyRepository = reviewReplyRepository;
		this.programRepository = programRepository;
		this.reservationRepository = reservationRepository;
		this.reviewCacheService = reviewCacheService;
		this.entityManager = entityManager;
	}

	@Transactional
	public ReviewResponseDto createReview(ReviewRequestDto requestDto, Member member, Long programId, Long reservationId) {
		log.info("리뷰 작성 시작 - memberId: {}, programId: {}", member.getId(), programId);

		// 프로그램, 예약 조회
		Program program = programRepository.findByIdOrElseThrow(programId);
		Reservation reservation = reservationRepository.findByIdOrElseThrow(reservationId);

		// 체험 완료 확인
		validateCompletedReservation(reservation, member);

		// 30일 이내 작성 가능 체크 - 임시로 현재 날짜 기준으로 체크
		// TODO: ProgramSchedule의 종료일 또는 Program의 종료일 기준으로 변경 필요
		validateReviewPeriod(LocalDate.now());

		// 글자수 제한 검증 - DTO @Valid로 이미 검증됨
		// validateContentLength(requestDto.getContent());

		// 중복 리뷰 체크
		if (reviewRepository.existsByMemberAndProgramAndDeletedStatus(member, program, DeletedStatus.DISPLAYED)) {
			throw new CustomException(ErrorCode.DUPLICATE_REVIEW);
		}

		Review review = Review.builder()
			.member(member)
			.program(program)
			.reservation(reservation)
			.content(requestDto.getContent())
			.build();

		Review savedReview = reviewRepository.save(review);

		// 캐시 무효화
		reviewCacheService.invalidateReviewCacheByPattern(programId);

		return new ReviewResponseDto(savedReview);
	}

	@Transactional(readOnly = true)
	public List<ReviewResponseDto> getMyReviews(Member member) {
		log.info("내 리뷰 조회 - memberId: {}", member.getId());

		List<Review> reviews = reviewRepository.findByMemberAndDeletedStatus(member, DeletedStatus.DISPLAYED);

		return reviews.stream()
			.map(ReviewResponseDto::new)
			.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<ReviewResponseDto> getProgramReviews(Long programId) {
		log.info("프로그램 리뷰 조회 - programId: {}", programId);

		Program program = programRepository.findByIdOrElseThrow(programId);

		List<Review> reviews = reviewRepository.findByProgramAndDeletedStatus(program, DeletedStatus.DISPLAYED);

		return reviews.stream()
			.map(ReviewResponseDto::new)
			.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<ReviewResponseDto> getAllReviews() {
		log.info("전체 리뷰 조회");

		List<Review> reviews = reviewRepository.findAllByDeletedStatus(DeletedStatus.DISPLAYED);

		return reviews.stream()
			.map(ReviewResponseDto::new)
			.collect(Collectors.toList());
	}

	@Transactional
	public ReviewResponseDto updateReview(Long reviewId, ReviewRequestDto requestDto, Member member) {
		log.info("리뷰 수정 - reviewId: {}, memberId: {}", reviewId, member.getId());

		// 글자수 제한 검증 - DTO @Valid로 이미 검증됨
		// validateContentLength(requestDto.getContent());

		Review review = reviewRepository.findByIdAndDeletedStatusOrThrow(reviewId, DeletedStatus.DISPLAYED);

		// 본인 확인
		if (!review.getMember().getId().equals(member.getId())) {
			throw new CustomException(ErrorCode.UNAUTHORIZED);
		}

		// 답글 존재 여부 확인
		if (reviewReplyRepository.existsByReviewAndDeletedStatus(review, DeletedStatus.DISPLAYED)) {
			throw new CustomException(ErrorCode.CANNOT_UPDATE_REVIEW_WITH_REPLY);
		}

		review.updateContent(requestDto.getContent());

		// 캐시 무효화
		reviewCacheService.invalidateReviewCacheByPattern(review.getProgram().getId());

		return new ReviewResponseDto(review);
	}

	@Transactional
	public void deleteReview(Long reviewId, Member member) {
		log.info("리뷰 삭제 - reviewId: {}, memberId: {}", reviewId, member.getId());

		Review review = reviewRepository.findByIdAndDeletedStatusOrThrow(reviewId, DeletedStatus.DISPLAYED);

		// 본인 확인
		if (!review.getMember().getId().equals(member.getId())) {
			throw new CustomException(ErrorCode.UNAUTHORIZED);
		}

		// Soft delete
		review.deleteReview();

		// 답글도 함께 soft delete
		reviewReplyRepository.softDeleteByReview(review);

		/**
		 * 영속성 컨텍스트를 최신 DB 상태로 동기화
		 * JPQL UPDATE 쿼리는 영속성 컨텍스트를 거치지 않으므로,
		 * 같은 트랜잭션에서 review.getReplies()를 다시 사용할 경우를 대비
		 */
		entityManager.refresh(review);

		// 캐시 무효화
		reviewCacheService.invalidateReviewCacheByPattern(review.getProgram().getId());
	}

	private void validateCompletedReservation(Reservation reservation, Member member) {
		if (!reservation.getMember().getId().equals(member.getId())) {
			throw new CustomException(ErrorCode.UNAUTHORIZED);
		}

		// TODO: 예약 완료 상태 확인 로직 - 현재는 COMPLETED 상태로 가정
		// 실제로는 아래 주석 해제하여 사용
		// if (reservation.getReservationStatus() != ReservationStatus.COMPLETED) {
		//     throw new CustomException(ErrorCode.REVIEW_NOT_ALLOWED);
		// }

		// 임시로 모든 예약을 완료 상태로 가정
		log.info("예약 상태 확인 - 임시로 COMPLETED 상태로 가정");
	}

	private void validateReviewPeriod(LocalDate experienceEndDate) {
		// TODO: 실제 체험 종료일 기준으로 변경 필요
		// 현재는 오늘 날짜 기준으로 30일 이내 항상 통과하도록 설정
		log.info("리뷰 작성 기간 확인 - 임시로 항상 통과");

		// 원래 로직 (나중에 활성화)
		// if (experienceEndDate.plusDays(30).isBefore(LocalDate.now())) {
		//     throw new CustomException(ErrorCode.REVIEW_PERIOD_EXPIRED);
		// }
	}
}
