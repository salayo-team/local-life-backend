package com.salayo.locallifebackend.domain.onboarding.repository;

import com.salayo.locallifebackend.domain.category.entity.AptitudeCategory;
import com.salayo.locallifebackend.domain.category.entity.RegionCategory;
import com.salayo.locallifebackend.domain.program.entity.Program;
import com.salayo.locallifebackend.domain.program.enums.ProgramStatus;
import com.salayo.locallifebackend.global.enums.DeletedStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * 온보딩 완료 후 프로그램 추천을 위한 Repository 인터페이스
 * domain/program 패키지와 분리하여 온보딩 도메인에서 관리
 */
public interface OnboardingProgramRepository {
    
    /**
     * 추천 프로그램 조회
     * 적성 카테고리와 지역 카테고리가 일치하고, 현재 운영 중인 프로그램 조회
     * 
     * @param aptitudeCategory 적성 카테고리
     * @param regionCategories 지역 카테고리 목록
     * @param status 프로그램 상태
     * @param deletedStatus 삭제 상태
     * @param currentDate 현재 날짜
     * @return 추천 프로그램 목록
     */
    List<Program> findRecommendedPrograms(
        AptitudeCategory aptitudeCategory,
        List<RegionCategory> regionCategories,
        ProgramStatus status,
        DeletedStatus deletedStatus,
        LocalDate currentDate
    );
}
