package com.salayo.locallifebackend.domain.review.repository;

import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.program.entity.Program;
import com.salayo.locallifebackend.domain.review.entity.Review;
import com.salayo.locallifebackend.global.enums.DeletedStatus;
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

	Optional<Review> findByIdAndDeletedStatus(Long reviewId, DeletedStatus deletedStatus);

	boolean existsByMemberAndProgramAndDeletedStatus(Member member, Program program, DeletedStatus deletedStatus);

	@Query("SELECT r FROM Review r WHERE r.member = :member AND r.deletedStatus = :deletedStatus ORDER BY r.createdAt DESC")
	List<Review> findByMemberAndDeletedStatus(@Param("member") Member member, @Param("deletedStatus") DeletedStatus deletedStatus);

	@Query("SELECT r FROM Review r WHERE r.program = :program AND r.deletedStatus = :deletedStatus ORDER BY r.createdAt DESC")
	List<Review> findByProgramAndDeletedStatus(@Param("program") Program program, @Param("deletedStatus") DeletedStatus deletedStatus);

	@Query("SELECT r FROM Review r WHERE r.deletedStatus = :deletedStatus ORDER BY r.createdAt DESC")
	List<Review> findAllByDeletedStatus(@Param("deletedStatus") DeletedStatus deletedStatus);

	@Query("SELECT r FROM Review r LEFT JOIN FETCH r.replies WHERE r.id = :reviewId AND r.deletedStatus = :deletedStatus")
	Optional<Review> findByIdAndDeletedStatusWithReplies(@Param("reviewId") Long reviewId,
		@Param("deletedStatus") DeletedStatus deletedStatus);

	default Review findByIdAndDeletedStatusOrThrow(Long reviewId, DeletedStatus deletedStatus) {
		return findByIdAndDeletedStatus(reviewId, deletedStatus)
			.orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));
	}
}
