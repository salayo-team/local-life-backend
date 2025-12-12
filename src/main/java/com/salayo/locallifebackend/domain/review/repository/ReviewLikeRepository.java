package com.salayo.locallifebackend.domain.review.repository;

import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.review.entity.Review;
import com.salayo.locallifebackend.domain.review.entity.ReviewLike;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {

	Optional<ReviewLike> findByReviewAndMember(Review review, Member member);

	boolean existsByReviewAndMember(Review review, Member member);
}
