package com.salayo.locallifebackend.domain.onboarding.service;

import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.member.repository.MemberRepository;
import com.salayo.locallifebackend.domain.onboarding.entity.OnboardingProgress;
import com.salayo.locallifebackend.domain.onboarding.entity.UserPreferredRegions;
import com.salayo.locallifebackend.domain.onboarding.enums.RegionType;
import com.salayo.locallifebackend.domain.onboarding.repository.OnboardingProgressRepository;
import com.salayo.locallifebackend.domain.onboarding.repository.UserPreferredRegionsRepository;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 사용자 선호 지역 관리 Service
 * 선호 지역 조회, 수정, 자동 매핑 기능 제공
 */
@Slf4j
@Service
public class UserPreferredRegionService {

    private final UserPreferredRegionsRepository userPreferredRegionsRepository;
    private final MemberRepository memberRepository;


    public UserPreferredRegionService(
            UserPreferredRegionsRepository userPreferredRegionsRepository,
            MemberRepository memberRepository) {
            this.userPreferredRegionsRepository = userPreferredRegionsRepository;
            this.memberRepository = memberRepository;
	}

    /**
     * 사용자의 선호 지역 목록 조회
     * 마이페이지에서 사용
     */
    @Transactional(readOnly = true)
    public List<String> getUserPreferredRegions(Long memberId) {

        Member member = memberRepository.findActiveByIdOrThrow(memberId);

        List<UserPreferredRegions> preferredRegions = 
            userPreferredRegionsRepository.findByMemberAndIsActiveTrue(member);

        if (preferredRegions.isEmpty()) {
            log.warn("선호 지역이 설정되지 않음 - memberId: {}", memberId);

            return new ArrayList<>();
        }

        return preferredRegions.stream()
            .map(UserPreferredRegions::getRegionName)
            .toList();
    }

    /**
     * 사용자 선호 지역 업데이트 (지역 특징 재선택)
     * 마이페이지에서 선호 지역 변경 시 사용
     */
    @Transactional
    public List<String> updateUserPreferredRegions(Long memberId,
        RegionType newRegionType, boolean isSameType) {

        Member member = memberRepository.findActiveByIdOrThrow(memberId);

        if (isSameType) {
            log.info("동일한 지역 특징 선택 - memberId: {}, regionType: {}", member.getId(), newRegionType);

            return newRegionType.getRegions();
        }

        // 기존 선호 지역 삭제
        userPreferredRegionsRepository.deleteAllByMember(member);

        // 새로운 지역 특징에 따른 지역 매핑
        List<String> regions = newRegionType.getRegions();
        List<UserPreferredRegions> newPreferredRegions = new ArrayList<>();
        for (String regionName : regions) {
            newPreferredRegions.add(UserPreferredRegions.builder()
                .member(member)
                .regionName(regionName)
                .isActive(true)
                .build());
        }
        userPreferredRegionsRepository.saveAll(newPreferredRegions);

        log.info("선호 지역 업데이트 완료 - memberId: {}, 새로운 regionType: {}, 매핑된 지역 수: {}",
            memberId, newRegionType, regions.size());
        log.debug("매핑된 지역 목록: {}", regions);

        return regions;
    }

    /**
     * 온보딩 과정에서 선호 지역 설정
     * OnboardingService에서 호출하여 사용
     */
    @Transactional
    public void setPreferredRegionsForOnboarding(Member member, RegionType regionType) {
        // 기존 선호 지역 삭제 (재선택 시)
        userPreferredRegionsRepository.deleteAllByMember(member);

        // 선택한 특징에 따른 지역 자동 매핑
        List<String> regions = regionType.getRegions();
        for (String regionName : regions) {
            UserPreferredRegions preferredRegion = UserPreferredRegions.builder()
                .member(member)
                .regionName(regionName)
                .isActive(true)
                .build();
            userPreferredRegionsRepository.save(preferredRegion);
        }

        log.info("온보딩 선호 지역 설정 완료 - memberId: {}, regionType: {}, 매핑된 지역 수: {}",
            member.getId(), regionType, regions.size());
    }

    /**
     * 선호 지역 특징별 지역 목록 조회
     * 참고용 API
     */
    public List<String> getRegionsByType(RegionType regionType) {
        return regionType.getRegions();
    }
}
