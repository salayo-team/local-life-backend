package com.salayo.locallifebackend.domain.review.repository;

import com.salayo.locallifebackend.domain.review.entity.Review;
import com.salayo.locallifebackend.domain.review.entity.ReviewReply;
import com.salayo.locallifebackend.global.enums.DeletedStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewReplyRepository extends JpaRepository<ReviewReply, Long> {

	boolean existsByReviewAndDeletedStatus(Review review, DeletedStatus deletedStatus);

}
