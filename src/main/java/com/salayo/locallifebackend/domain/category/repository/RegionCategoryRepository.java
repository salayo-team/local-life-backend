package com.salayo.locallifebackend.domain.category.repository;

import com.salayo.locallifebackend.domain.category.entity.RegionCategory;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegionCategoryRepository extends JpaRepository<RegionCategory, Long> {

	default RegionCategory findByIdOrElseThrow(Long regionCategoryId){
		return findById(regionCategoryId).orElseThrow(() -> new CustomException(ErrorCode.REGION_NOT_FOUND));
	};

	boolean existsByRegionName(String regionName);
	
	/**
	 * 여러 지역명으로 지역 카테고리 조회
	 */
	List<RegionCategory> findByRegionNameIn(List<String> regionNames);
}
