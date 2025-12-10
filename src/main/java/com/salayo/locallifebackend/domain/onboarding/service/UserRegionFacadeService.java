package com.salayo.locallifebackend.domain.onboarding.service;

import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.member.repository.MemberRepository;
import com.salayo.locallifebackend.domain.onboarding.dto.OnboardingProgressResponseDto;
import com.salayo.locallifebackend.domain.onboarding.dto.OnboardingRegionRequestDto;
import com.salayo.locallifebackend.domain.onboarding.enums.RegionType;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserRegionFacadeService {

	private final OnboardingService onboardingService;
	private final UserPreferredRegionService userPreferredRegionService;
	private final MemberRepository memberRepository;

	public UserRegionFacadeService(OnboardingService onboardingService,
		UserPreferredRegionService userPreferredRegionService,
		MemberRepository memberRepository) {
		this.onboardingService = onboardingService;
		this.userPreferredRegionService = userPreferredRegionService;
		this.memberRepository = memberRepository;
	}

	@Transactional
	public List<String> getUserPreferredRegions(Long memberId) {

		onboardingService.getCompletedOnboardingProgress(memberId);

		return userPreferredRegionService.getUserPreferredRegions(memberId);
	}

	@Transactional
	public List<String> updateUserPreferredRegions(Long memberId, RegionType newRegionType) {
		Member member = memberRepository.findActiveByIdOrThrow(memberId);

		boolean isSameType = onboardingService.checkAndUpdateRegionTypeForMypage(memberId, newRegionType);

		return userPreferredRegionService.updateUserPreferredRegions(memberId, newRegionType, isSameType);
	}

	/**
	 * 온보딩 중 선호 지역 선택 플로우를 총괄 두 서비스를 조율(Orchestration)하는 메서드
	 */
	@Transactional
	public OnboardingProgressResponseDto coordinateSelectRegion(Long memberId,
		OnboardingRegionRequestDto regionRequestDto) {

		OnboardingProgressResponseDto onboardingProgressResponseDto = onboardingService.selectRegion(memberId, regionRequestDto);

		Member member = memberRepository.findActiveByIdOrThrow(memberId);
		RegionType regionType = regionRequestDto.regionType();

		userPreferredRegionService.setPreferredRegionsForOnboarding(member, regionType);
		log.info("Facade: 선호 지역 선택 플로우 조율 완료 - memberId: {}", memberId);

		return onboardingProgressResponseDto;
	}
}
