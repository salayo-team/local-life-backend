package com.salayo.locallifebackend.domain.review.service;

import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.program.entity.Program;
import com.salayo.locallifebackend.domain.program.repository.ProgramRepository;
import com.salayo.locallifebackend.domain.programschedule.entity.ProgramSchedule;
import com.salayo.locallifebackend.domain.reservation.entity.Reservation;
import com.salayo.locallifebackend.domain.reservation.enums.ReservationStatus;
import com.salayo.locallifebackend.domain.reservation.repository.ReservationRepository;
import com.salayo.locallifebackend.domain.review.dto.ReviewRequestDto;
import com.salayo.locallifebackend.domain.review.dto.ReviewResponseDto;
import com.salayo.locallifebackend.domain.review.dto.ReviewSearchRequestDto;
import com.salayo.locallifebackend.domain.review.dto.ReviewTagResponseDto;
import com.salayo.locallifebackend.domain.review.entity.Review;
import com.salayo.locallifebackend.domain.review.entity.ReviewLike;
import com.salayo.locallifebackend.domain.review.entity.ReviewTag;
import com.salayo.locallifebackend.domain.review.entity.ReviewTagMapping;
import com.salayo.locallifebackend.domain.review.repository.ReviewLikeRepository;
import com.salayo.locallifebackend.domain.review.repository.ReviewReplyRepository;
import com.salayo.locallifebackend.domain.review.repository.ReviewRepository;
import com.salayo.locallifebackend.domain.review.repository.ReviewTagMappingRepository;
import com.salayo.locallifebackend.domain.review.repository.ReviewTagRepository;
import com.salayo.locallifebackend.global.dto.PaginationResponseDto;
import com.salayo.locallifebackend.global.enums.DeletedStatus;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ReviewService {

	private final ReviewRepository reviewRepository;
	private final ReviewReplyRepository reviewReplyRepository;
	private final ReviewLikeRepository reviewLikeRepository;
	private final ReviewTagRepository reviewTagRepository;
	private final ReviewTagMappingRepository reviewTagMappingRepository;
	private final ProgramRepository programRepository;
	private final ReservationRepository reservationRepository;
	private final ReviewCacheService reviewCacheService;

	public ReviewService(ReviewRepository reviewRepository, ReviewReplyRepository reviewReplyRepository,
		ReviewLikeRepository reviewLikeRepository, ReviewTagRepository reviewTagRepository,
		ReviewTagMappingRepository reviewTagMappingRepository, ProgramRepository programRepository,
		ReservationRepository reservationRepository, ReviewCacheService reviewCacheService) {
		this.reviewRepository = reviewRepository;
		this.reviewReplyRepository = reviewReplyRepository;
		this.reviewLikeRepository = reviewLikeRepository;
		this.reviewTagRepository = reviewTagRepository;
		this.reviewTagMappingRepository = reviewTagMappingRepository;
		this.programRepository = programRepository;
		this.reservationRepository = reservationRepository;
		this.reviewCacheService = reviewCacheService;
	}

	@Transactional
	public ReviewResponseDto createReview(ReviewRequestDto requestDto, Member member, Long programId, Long reservationId) {
		log.info("리뷰 작성 시작 - memberId: {}, programId: {}", member.getId(), programId);

		Program program = programRepository.findByIdOrElseThrow(programId);
		Reservation reservation = reservationRepository.findByIdOrElseThrow(reservationId);

		ProgramSchedule programSchedule = reservation.getProgramSchedule();

		if (programSchedule == null) {
			throw new CustomException(ErrorCode.INVALID_REQUEST, "예약에 연결된 스케줄 정보가 없습니다.");
		}
		validateReservationAssociation(program, reservation);
		validateCompletedReservation(reservation, member);
		validateReviewPeriod(program.getEndDate());

		if (reviewRepository.existsByMemberAndProgramAndDeletedStatus(member, program, DeletedStatus.DISPLAYED)) {
			throw new CustomException(ErrorCode.DUPLICATE_REVIEW);
		}
		Review review = Review.builder()
			.member(member)
			.program(program)
			.reservation(reservation)
			.content(requestDto.getContent())
			.rating(requestDto.getReviewRating())
			.build();

		Review savedReview = reviewRepository.save(review);
		saveReviewTags(savedReview, requestDto.getTagIds());

		updateReviewCountAndAverageRatingOnCreate(program, savedReview.getReviewRating());
		log.info("리뷰 생성 완료 및 리뷰 평균 평점 업데이트 - reviewId: {}, programId: {}", savedReview.getId(), programId);

		reviewCacheService.invalidateReviewCacheByPattern(programId);

		return new ReviewResponseDto(savedReview);
	}

	@Transactional(readOnly = true)
	public PaginationResponseDto<ReviewResponseDto> getMyReviews(Member member, Pageable pageable) {
		log.info("내 리뷰 페이징 조회 - memberId: {}, page: {}", member.getId(), pageable.getPageNumber());

		Page<Review> reviewPage = reviewRepository.findByMemberAndDeletedStatus(member, DeletedStatus.DISPLAYED, pageable);
		Page<ReviewResponseDto> dtoPage = reviewPage.map(ReviewResponseDto::new);

		return PaginationResponseDto.of(dtoPage);
	}

	@Transactional(readOnly = true)
	public PaginationResponseDto<ReviewResponseDto> getProgramReviews(Long programId, Pageable pageable) {
		log.info("프로그램 리뷰 페이징 조회 - programId: {}, page: {}", programId, pageable.getPageNumber());

		Program program = programRepository.findByIdOrElseThrow(programId);
		Page<Review> reviewPage = reviewRepository.findByProgramAndDeletedStatus(program, DeletedStatus.DISPLAYED, pageable);
		Page<ReviewResponseDto> dtoPage = reviewPage.map(ReviewResponseDto::new);

		return PaginationResponseDto.of(dtoPage);
	}

	@Transactional
	public ReviewResponseDto getReviewDetail(Long reviewId) {
		log.info("리뷰 상세 조회 - reviewId: {}", reviewId);

		Review review = reviewRepository.findByIdAndDeletedStatusOrThrow(reviewId, DeletedStatus.DISPLAYED);
		review.incrementViewCount();

		return new ReviewResponseDto(review);
	}

	@Transactional
	public void likeReview(Long reviewId, Member member) {
		log.info("리뷰 좋아요 - reviewId: {}, memberId: {}", reviewId, member.getId());

		Review review = reviewRepository.findByIdAndDeletedStatusOrThrow(reviewId, DeletedStatus.DISPLAYED);

		if (reviewLikeRepository.existsByReviewAndMember(review, member)) {
			throw new CustomException(ErrorCode.ALREADY_LIKED_REVIEW);
		}

		ReviewLike reviewLike = ReviewLike.builder()
			.review(review)
			.member(member)
			.build();

		reviewLikeRepository.save(reviewLike);
		review.incrementLikeCount();
	}

	@Transactional
	public void unlikeReview(Long reviewId, Member member) {
		log.info("리뷰 좋아요 취소 - reviewId: {}, memberId: {}", reviewId, member.getId());

		Review review = reviewRepository.findByIdAndDeletedStatusOrThrow(reviewId, DeletedStatus.DISPLAYED);

		ReviewLike reviewLike = reviewLikeRepository.findByReviewAndMember(review, member)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_LIKED_REVIEW));

		reviewLikeRepository.delete(reviewLike);
		review.decrementLikeCount();
	}

	@Transactional(readOnly = true)
	public List<ReviewTagResponseDto> getAllTags() {
		log.info("전체 리뷰 태그 조회");

		return reviewTagRepository.findByTagIsActiveTrueOrderByDisplayOrderAsc().stream()
			.map(ReviewTagResponseDto::new)
			.toList();
	}

	@Transactional(readOnly = true)
	public PaginationResponseDto<ReviewResponseDto> searchReviews(ReviewSearchRequestDto requestDto) {
		log.info("리뷰 필터링 검색 - tagIds: {}, keyword: {}", requestDto.getTagIds(), requestDto.getKeyword());

		Page<Review> reviewPage = reviewRepository.searchReviews(requestDto);
		Page<ReviewResponseDto> dtoPage = reviewPage.map(ReviewResponseDto::new);

		return PaginationResponseDto.of(dtoPage);
	}

	@Transactional(readOnly = true)
	public PaginationResponseDto<ReviewResponseDto> getAllReviews(Pageable pageable) {
		log.info("전체 리뷰 페이징 조회 - page: {}", pageable.getPageNumber());

		Page<Review> reviewPage = reviewRepository.findAllByDeletedStatus(DeletedStatus.DISPLAYED, pageable);
		Page<ReviewResponseDto> dtoPage = reviewPage.map(ReviewResponseDto::new);

		return PaginationResponseDto.of(dtoPage);
	}

	@Transactional
	public ReviewResponseDto updateReview(Long reviewId, ReviewRequestDto requestDto, Member member) {
		log.info("리뷰 수정 - reviewId: {}, memberId: {}", reviewId, member.getId());

		Review review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

		if (!review.getMember().getId().equals(member.getId())) {
			throw new CustomException(ErrorCode.FORBIDDEN_ACCESS);
		}

		if (reviewReplyRepository.existsByReviewAndDeletedStatus(review, DeletedStatus.DISPLAYED)) {
			throw new CustomException(ErrorCode.CANNOT_UPDATE_REVIEW_WITH_REPLY);
		}
		BigDecimal oldRating = review.getReviewRating();

		review.updateContent(requestDto.getContent());
		review.updateRating(requestDto.getReviewRating());
		updateReviewTags(review, requestDto.getTagIds());

		updateReviewCountAndAverageRatingOnUpdate(review.getProgram(),
			oldRating, requestDto.getReviewRating());

		reviewCacheService.invalidateReviewCacheByPattern(review.getProgram().getId());

		return new ReviewResponseDto(review);
	}

	@Transactional
	public void deleteReview(Long reviewId, Member member) {
		log.info("리뷰 삭제 시작 - reviewId: {}, memberId: {}", reviewId, member.getId());

		Review review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

		if (!review.getMember().getId().equals(member.getId())) {
			throw new CustomException(ErrorCode.FORBIDDEN_ACCESS);
		}
		BigDecimal deletedRating = review.getReviewRating();
		Program program = review.getProgram();

		review.softDelete();
		log.info("리뷰 및 관련 답글 소프트 삭제 완료 - reviewId: {}", reviewId);

		updateReviewCountAndAverageRatingOnDelete(program, deletedRating);
		log.info("리뷰 평균 평점 업데이트 완료 - programId: {}", program.getId());

		reviewCacheService.invalidateReviewCacheByPattern(review.getProgram().getId());
	}

	private void saveReviewTags(Review review, List<Long> tagIds) {
		if (tagIds == null || tagIds.isEmpty()) {
			return;
		}

		List<ReviewTag> tags = reviewTagRepository.findByIdIn(tagIds);

		List<ReviewTagMapping> mappings = tags.stream()
			.map(tag -> ReviewTagMapping.builder()
				.review(review)
				.reviewTag(tag)
				.build())
			.toList();

		reviewTagMappingRepository.saveAll(mappings);
	}

	private void updateReviewTags(Review review, List<Long> tagIds) {
		reviewTagMappingRepository.deleteByReview(review);
		reviewTagMappingRepository.flush();

		if (tagIds != null && !tagIds.isEmpty()) {
			saveReviewTags(review, tagIds);
		}
	}

	/**
	 * 예약이 해당 프로그램에 속해 있는지 검증하는 메서드
	 */
	private void validateReservationAssociation(Program program, Reservation reservation) {
		if (!reservation.getProgramSchedule().getProgram().getId().equals(program.getId())) {
			throw new CustomException(ErrorCode.INVALID_REQUEST, "해당 프로그램에 대한 예약 정보가 아닙니다.");
		}
	}

	private void validateCompletedReservation(Reservation reservation, Member member) {
		if (!reservation.getMember().getId().equals(member.getId())) {
			throw new CustomException(ErrorCode.FORBIDDEN_ACCESS, "자신의 예약에 대해서만 리뷰를 작성할 수 있습니다.");
		}

		if (reservation.getReservationStatus() != ReservationStatus.COMPLETED) {
			throw new CustomException(ErrorCode.REVIEW_NOT_ALLOWED, "체험이 완료된 예약에 대해서만 리뷰를 작성할 수 있습니다.");
		}
	}

	private void validateReviewPeriod(LocalDate experienceEndDate) {

		if (LocalDate.now().isAfter(experienceEndDate.plusDays(30))) {
			throw new CustomException(ErrorCode.REVIEW_PERIOD_EXPIRED);
		}

	}

	/**
	 * 새로운 리뷰가 추가되었을 때 Program의 리뷰 개수와 리뷰 평균 평점을 업데이트
	 *
	 * @param program   평점을 업데이트할 프로그램
	 * @param newRating 새로 추가된 리뷰의 별점
	 */
	private void updateReviewCountAndAverageRatingOnCreate(Program program, BigDecimal newRating) {
		int currentReviewCount = program.getReviewCount();
		BigDecimal currentAverageRating = program.getAverageRating();

		BigDecimal totalRating = currentAverageRating.multiply(new BigDecimal(currentReviewCount));
		int newReviewCount = currentReviewCount + 1;
		BigDecimal newAverageRating = totalRating.add(newRating)
			.divide(new BigDecimal(newReviewCount), 1, RoundingMode.HALF_UP);

		program.updateReviewStatus(newReviewCount, newAverageRating);
	}

	/**
	 * 리뷰 수정 시 Program의 리뷰 평균 평점을 업데이트하는 메서드
	 *
	 * @param program   평점을 업데이트할 프로그램
	 * @param oldRating 수정 전 별점
	 * @param newRating 수정 후 별점
	 */
	private void updateReviewCountAndAverageRatingOnUpdate(Program program, BigDecimal oldRating, BigDecimal newRating) {
		if (oldRating.compareTo(newRating) == 0) {
			return;
		}

		int reviewCount = program.getReviewCount();
		if (reviewCount <= 0) {
			return;
		}

		BigDecimal totalRating = program.getAverageRating().multiply(new BigDecimal(reviewCount));
		BigDecimal newAverageRating = totalRating.subtract(oldRating).add(newRating)
			.divide(new BigDecimal(reviewCount), 1, RoundingMode.HALF_UP);

		program.updateReviewStatus(reviewCount, newAverageRating);
	}

	/**
	 * 리뷰가 삭제되었을 때 Program의 리뷰 개수와 리뷰 평균 평점을 업데이트
	 *
	 * @param program       평점을 업데이트할 프로그램
	 * @param deletedRating 삭제된 리뷰의 별점
	 */
	private void updateReviewCountAndAverageRatingOnDelete(Program program, BigDecimal deletedRating) {
		int currentReviewCount = program.getReviewCount();
		BigDecimal currentAverageRating = program.getAverageRating();

		BigDecimal totalRating = currentAverageRating.multiply(new BigDecimal(currentReviewCount));
		int newReviewCount = currentReviewCount - 1;

		BigDecimal newAverageRating;
		if (newReviewCount > 0) {
			newAverageRating = totalRating.subtract(deletedRating)
				.divide(new BigDecimal(newReviewCount), 1, RoundingMode.HALF_UP);
		} else {
			newAverageRating = BigDecimal.ZERO.setScale(1);
		}

		program.updateReviewStatus(newReviewCount, newAverageRating);
	}
}
