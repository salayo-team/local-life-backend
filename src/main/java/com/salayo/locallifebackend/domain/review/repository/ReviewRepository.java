package com.salayo.locallifebackend.domain.review.repository;

import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.program.entity.Program;
import com.salayo.locallifebackend.domain.review.entity.Review;
import com.salayo.locallifebackend.global.enums.DeletedStatus;
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
public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewCustomRepository {

	Optional<Review> findByIdAndDeletedStatus(Long reviewId, DeletedStatus deletedStatus);

	boolean existsByMemberAndProgramAndDeletedStatus(Member member, Program program, DeletedStatus deletedStatus);

	@Query("SELECT r FROM Review r WHERE r.member = :member AND r.deletedStatus = :deletedStatus")
	Page<Review> findByMemberAndDeletedStatus(@Param("member") Member member, @Param("deletedStatus") DeletedStatus deletedStatus,
		Pageable pageable);

	@Query("SELECT r FROM Review r WHERE r.program = :program AND r.deletedStatus = :deletedStatus")
	Page<Review> findByProgramAndDeletedStatus(@Param("program") Program program, @Param("deletedStatus") DeletedStatus deletedStatus,
		Pageable pageable);

	@Query("SELECT r FROM Review r WHERE r.deletedStatus = :deletedStatus")
	Page<Review> findAllByDeletedStatus(@Param("deletedStatus") DeletedStatus deletedStatus, Pageable pageable);

	default Review findByIdAndDeletedStatusOrThrow(Long reviewId, DeletedStatus deletedStatus) {
		return findByIdAndDeletedStatus(reviewId, deletedStatus)
			.orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));
	}
}
