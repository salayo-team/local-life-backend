package com.salayo.locallifebackend.domain.review.repository;

import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.program.entity.Program;
import com.salayo.locallifebackend.domain.review.entity.Review;
import com.salayo.locallifebackend.domain.review.enums.ReviewStatus;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import com.salayo.locallifebackend.global.error.ErrorCode;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

	Optional<Review> findByIdAndStatus(Long reviewId, ReviewStatus status);

	boolean existsByMemberAndProgramAndStatus(Member member, Program program, ReviewStatus status);

	@Query("SELECT r FROM Review r WHERE r.member = :member AND r.status = :status ORDER BY r.createdAt DESC")
	Page<Review> findByMemberAndStatus(@Param("member") Member member, @Param("status") ReviewStatus status, Pageable pageable);

	@Query("SELECT r FROM Review r WHERE r.program = :program AND r.status = :status ORDER BY r.createdAt DESC")
	Page<Review> findByProgramAndStatus(@Param("program") Program program, @Param("status") ReviewStatus status, Pageable pageable);

	@Query("SELECT r FROM Review r WHERE r.status = :status ORDER BY r.createdAt DESC")
	Page<Review> findAllByStatus(@Param("status") ReviewStatus status, Pageable pageable);

	@Query("SELECT r FROM Review r LEFT JOIN FETCH r.replies WHERE r.id = :reviewId AND r.status = :status")
	Optional<Review> findByIdAndStatusWithReplies(@Param("reviewId") Long reviewId, @Param("status") ReviewStatus status);

	default Review findByIdAndStatusOrThrow(Long reviewId, ReviewStatus status) {
		return findByIdAndStatus(reviewId, status)
			.orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));
	}
}
