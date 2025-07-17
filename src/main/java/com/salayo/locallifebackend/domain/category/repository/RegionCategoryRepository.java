package com.salayo.locallifebackend.domain.category.repository;

import com.salayo.locallifebackend.domain.category.entity.RegionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegionCategoryRepository extends JpaRepository<RegionCategory, Long> {

	RegionCategory findByIdOrElseThrow(Long regionCategoryId);
}
