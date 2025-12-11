package com.salayo.locallifebackend.domain.review.repository;

import com.salayo.locallifebackend.domain.review.entity.Review;
import com.salayo.locallifebackend.domain.review.entity.ReviewTagMapping;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewTagMappingRepository extends JpaRepository<ReviewTagMapping, Long> {

	List<ReviewTagMapping> findByReview(Review review);

	void deleteByReview(Review review);
}
