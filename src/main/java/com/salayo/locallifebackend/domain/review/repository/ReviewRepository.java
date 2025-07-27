package com.salayo.locallifebackend.domain.review.repository;

import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.program.entity.Program;
import com.salayo.locallifebackend.domain.review.entity.Review;
import com.salayo.locallifebackend.domain.review.enums.ReviewStatus;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import com.salayo.locallifebackend.global.error.ErrorCode;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

	Optional<Review> findByIdAndReviewStatus(Long reviewId, ReviewStatus reviewStatus);

	boolean existsByMemberAndProgramAndReviewStatus(Member member, Program program, ReviewStatus reviewStatus);

	@Query("SELECT r FROM Review r WHERE r.member = :member AND r.reviewStatus = :reviewStatus ORDER BY r.createdAt DESC")
	List<Review> findByMemberAndReviewStatus(@Param("member") Member member, @Param("reviewStatus") ReviewStatus reviewStatus);

	@Query("SELECT r FROM Review r WHERE r.program = :program AND r.reviewStatus = :reviewStatus ORDER BY r.createdAt DESC")
	List<Review> findByProgramAndReviewStatus(@Param("program") Program program, @Param("reviewStatus") ReviewStatus reviewStatus);

	@Query("SELECT r FROM Review r WHERE r.reviewStatus = :reviewStatus ORDER BY r.createdAt DESC")
	List<Review> findAllByReviewStatus(@Param("reviewStatus") ReviewStatus reviewStatus);

	@Query("SELECT r FROM Review r LEFT JOIN FETCH r.replies WHERE r.id = :reviewId AND r.reviewStatus = :reviewStatus")
	Optional<Review> findByIdAndReviewStatusWithReplies(@Param("reviewId") Long reviewId, @Param("reviewStatus") ReviewStatus reviewStatus);

	default Review findByIdAndReviewStatusOrThrow(Long reviewId, ReviewStatus reviewStatus) {
		return findByIdAndReviewStatus(reviewId, reviewStatus)
			.orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));
	}
}
