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
		// 온보딩 완료 여부 검증 (온보딩 도메인 책임)
		onboardingService.getCompletedOnboardingProgress(memberId);

		// 실제 데이터 조회는 region 서비스에 위임
		return userPreferredRegionService.getUserPreferredRegions(memberId);
	}

	@Transactional
	public List<String> updateUserPreferredRegions(Long memberId, RegionType newRegionType) {
		Member member = memberRepository.findActiveByIdOrThrow(memberId);

		// 온보딩 도메인 규칙: RegionType 변경 가능 여부 판단 + progress 업데이트
		boolean isSameType = onboardingService.checkAndUpdateRegionTypeForMypage(memberId, newRegionType);

		// 선호 지역 데이터 저장 담당
		return userPreferredRegionService.updateUserPreferredRegions(memberId, newRegionType, isSameType);
	}

	/**
	 * 온보딩 중 선호 지역 선택 플로우를 총괄
	 * 두 서비스를 조율(Orchestration)하는 메서드
	 */
	@Transactional
	public OnboardingProgressResponseDto coordinateSelectRegion(Long memberId,
		OnboardingRegionRequestDto regionRequestDto) {

		// 온보딩 전문가에게 '절차 진행'을 지시
		OnboardingProgressResponseDto onboardingProgressResponseDto = onboardingService.selectRegion(memberId, regionRequestDto);

		// 선호 지역 전문가에게 '데이터 생성'을 지시
		Member member = memberRepository.findActiveByIdOrThrow(memberId);
		RegionType regionType = regionRequestDto.regionType();

		userPreferredRegionService.setPreferredRegionsForOnboarding(member, regionType);
		log.info("Facade: 선호 지역 선택 플로우 조율 완료 - memberId: {}", memberId);

		// 최종적으로 컨트롤러에 응답을 반환
		return onboardingProgressResponseDto;
	}
}
