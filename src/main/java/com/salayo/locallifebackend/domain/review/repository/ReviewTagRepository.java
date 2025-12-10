package com.salayo.locallifebackend.domain.review.repository;

import com.salayo.locallifebackend.domain.review.entity.ReviewTag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewTagRepository extends JpaRepository<ReviewTag, Long> {

	List<ReviewTag> findByIsActiveTrueOrderByDisplayOrderAsc();

	List<ReviewTag> findByIdIn(List<Long> ids);
}
