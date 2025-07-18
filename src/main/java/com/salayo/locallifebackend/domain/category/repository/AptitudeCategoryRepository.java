package com.salayo.locallifebackend.domain.category.repository;

import com.salayo.locallifebackend.domain.category.entity.AptitudeCategory;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AptitudeCategoryRepository extends JpaRepository<AptitudeCategory, Long> {

	default AptitudeCategory findByIdOrElseThrow(Long aptitudeCategoryId){
		return findById(aptitudeCategoryId).orElseThrow(() -> new CustomException(ErrorCode.APTITUDE_NOT_FOUND));
	};
}
