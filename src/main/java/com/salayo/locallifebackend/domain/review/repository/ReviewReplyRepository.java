package com.salayo.locallifebackend.domain.review.repository;

import com.salayo.locallifebackend.domain.review.entity.Review;
import com.salayo.locallifebackend.domain.review.entity.ReviewReply;
import com.salayo.locallifebackend.domain.review.enums.ReviewStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewReplyRepository extends JpaRepository<ReviewReply, Long> {

	boolean existsByReviewAndStatus(Review review, ReviewStatus status);

	Optional<ReviewReply> findByReviewAndStatus(Review review, ReviewStatus status);

	@Modifying
	@Query("UPDATE ReviewReply r SET r.status = com.salayo.locallifebackend.domain.review.enums.ReviewStatus.DELETED, r.deletedAt = CURRENT_TIMESTAMP WHERE r.review = :review")
	void softDeleteByReview(@Param("review") Review review);

}
