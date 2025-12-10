package com.salayo.locallifebackend.domain.review.repository;

import com.salayo.locallifebackend.domain.review.dto.ReviewSearchRequestDto;
import com.salayo.locallifebackend.domain.review.entity.Review;
import org.springframework.data.domain.Page;

public interface ReviewCustomRepository {

	Page<Review> searchReviews(ReviewSearchRequestDto requestDto);
}
