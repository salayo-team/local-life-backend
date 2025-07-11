package com.salayo.locallifebackend.domain.category.repository;

import com.salayo.locallifebackend.domain.category.entity.AptitudeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AptitudeCategoryRepository extends JpaRepository {

	AptitudeCategory findByIdOrElseThrow(Long aptitudeCategoryId);
}
