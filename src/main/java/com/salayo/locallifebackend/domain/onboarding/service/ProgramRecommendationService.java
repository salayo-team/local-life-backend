package com.salayo.locallifebackend.domain.onboarding.service;

import com.salayo.locallifebackend.domain.ai.aptitude.entity.UserAptitude;
import com.salayo.locallifebackend.domain.ai.aptitude.repository.UserAptitudeRepository;
import com.salayo.locallifebackend.domain.category.entity.AptitudeCategory;
import com.salayo.locallifebackend.domain.category.entity.RegionCategory;
import com.salayo.locallifebackend.domain.category.repository.AptitudeCategoryRepository;
import com.salayo.locallifebackend.domain.category.repository.RegionCategoryRepository;
import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.member.repository.MemberRepository;
import com.salayo.locallifebackend.domain.onboarding.dto.RecommendedProgramResponseDto;
import com.salayo.locallifebackend.domain.onboarding.entity.UserPreferredRegions;
import com.salayo.locallifebackend.domain.onboarding.repository.UserPreferredRegionsRepository;
import com.salayo.locallifebackend.domain.program.entity.Program;
import com.salayo.locallifebackend.domain.program.enums.ProgramStatus;
import com.salayo.locallifebackend.domain.onboarding.repository.OnboardingProgramRepository;
import com.salayo.locallifebackend.global.enums.DeletedStatus;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 체험 프로그램 추천 서비스 온보딩 완료 후 사용자 맞춤 프로그램 6개 추천
 */
@Slf4j
@Service
public class ProgramRecommendationService {

	private final OnboardingProgramRepository onboardingProgramRepository;
	private final UserAptitudeRepository userAptitudeRepository;
	private final UserPreferredRegionsRepository userPreferredRegionsRepository;
	private final AptitudeCategoryRepository aptitudeCategoryRepository;
	private final RegionCategoryRepository regionCategoryRepository;
	private final MemberRepository memberRepository;

	public ProgramRecommendationService(
		OnboardingProgramRepository onboardingProgramRepository,
		UserAptitudeRepository userAptitudeRepository,
		UserPreferredRegionsRepository userPreferredRegionsRepository,
		AptitudeCategoryRepository aptitudeCategoryRepository,
		RegionCategoryRepository regionCategoryRepository,
		MemberRepository memberRepository) {
		this.onboardingProgramRepository = onboardingProgramRepository;
		this.userAptitudeRepository = userAptitudeRepository;
		this.userPreferredRegionsRepository = userPreferredRegionsRepository;
		this.aptitudeCategoryRepository = aptitudeCategoryRepository;
		this.regionCategoryRepository = regionCategoryRepository;
		this.memberRepository = memberRepository;
	}

	/**
	 * 온보딩 완료 후 추천 프로그램 6개 조회 사용자의 적성과 선호 지역에 맞는 프로그램 추천
	 */
	@Transactional(readOnly = true)
	public List<RecommendedProgramResponseDto> getRecommendedPrograms(Long memberId) {
		Member member = memberRepository.findActiveByIdOrThrow(memberId);

		UserAptitude userAptitude = userAptitudeRepository.findTopByMemberOrderByCreatedAtDesc(member)
			.orElseThrow(() -> new CustomException(ErrorCode.APTITUDE_NOT_FOUND));

		List<UserPreferredRegions> preferredRegions =
			userPreferredRegionsRepository.findByMemberAndIsActiveTrue(member);

		if (preferredRegions.isEmpty()) {
			log.warn("사용자의 선호 지역이 없습니다. memberId: {}", memberId);
			return Collections.emptyList();
		}

		List<String> regionNames = preferredRegions.stream()
			.map(UserPreferredRegions::getRegionName)
			.collect(Collectors.toList());

		AptitudeCategory aptitudeCategory = aptitudeCategoryRepository
			.findByAptitudeCode(userAptitude.getAptitudeType().name())
			.orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

		List<RegionCategory> regionCategories = regionCategoryRepository
			.findByRegionNameIn(regionNames);

		if (regionCategories.isEmpty()) {
			log.warn("매칭되는 지역 카테고리가 없습니다. regionNames: {}", regionNames);
			return Collections.emptyList();
		}

		List<Program> programs = onboardingProgramRepository.findRecommendedPrograms(
			aptitudeCategory,
			regionCategories,
			ProgramStatus.REGISTERED,
			DeletedStatus.DISPLAYED,
			LocalDate.now()
		);

		if (programs.isEmpty()) {
			log.info("추천 조건에 맞는 프로그램이 없습니다. memberId: {}, aptitude: {}, regions: {}",
				memberId, userAptitude.getAptitudeType(), regionNames);
			return Collections.emptyList();
		}

		List<RecommendedProgramResponseDto> result = programs.stream()
			.map(this::convertToDto)
			.collect(Collectors.toList());

		log.info("프로그램 추천 완료 - memberId: {}, 추천 개수: {}, 적성: {}, 지역: {}",
			memberId, result.size(), userAptitude.getAptitudeType(), regionNames);

		return result;
	}

	/**
	 * Program Entity를 DTO로 변환
	 */
	private RecommendedProgramResponseDto convertToDto(Program program) {
		return RecommendedProgramResponseDto.builder()
			.programId(program.getId())
			.title(program.getTitle())
			.businessName(program.getBusinessName())
			.description(program.getDescription())
			.location(program.getLocation())
			.price(program.getPrice())
			.finalPrice(program.getFinalPrice())
			.percent(program.getPercent())
			.maxCapacity(program.getMaxCapacity())
			.minCapacity(program.getMinCapacity())
			.startDate(program.getStartDate())
			.endDate(program.getEndDate())
			.aptitudeCategory(program.getAptitudeCategory().getAptitudeName())
			.regionCategory(program.getRegionCategory().getRegionName())
			.isLocalSpecialized(program.getIsLocalSpecialized())
			.build();
	}
}
