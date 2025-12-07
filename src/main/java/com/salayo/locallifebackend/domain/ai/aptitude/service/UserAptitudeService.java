package com.salayo.locallifebackend.domain.ai.aptitude.service;

import com.salayo.locallifebackend.domain.ai.aptitude.dto.UserAptitudeResponseDto;
import com.salayo.locallifebackend.domain.ai.aptitude.entity.UserAptitude;
import com.salayo.locallifebackend.domain.ai.aptitude.enums.AptitudeType;
import com.salayo.locallifebackend.domain.ai.aptitude.repository.UserAptitudeRepository;
import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.member.repository.MemberRepository;
import com.salayo.locallifebackend.domain.onboarding.entity.OnboardingProgress;
import com.salayo.locallifebackend.domain.onboarding.repository.OnboardingProgressRepository;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UserAptitudeService {

	private final UserAptitudeRepository userAptitudeRepository;
	private final MemberRepository memberRepository;
	private final OnboardingProgressRepository onboardingProgressRepository;

	public UserAptitudeService(UserAptitudeRepository userAptitudeRepository,
		MemberRepository memberRepository, OnboardingProgressRepository onboardingProgressRepository) {
		this.userAptitudeRepository = userAptitudeRepository;
		this.memberRepository = memberRepository;
		this.onboardingProgressRepository = onboardingProgressRepository;
	}

	/**
	 * 마이페이지 - 내 적성 조회
	 */
	@Transactional(readOnly = true)
	public UserAptitudeResponseDto getMyAptitude(Long memberId) {
		Member member = memberRepository.findActiveByIdOrThrow(memberId);

		// 온보딩 완료 여부 확인 (안전장치)
		checkOnboardingCompleted(member);

		// UserAptitude 조회, 없으면 PENDING 상태로 응답
		UserAptitude userAptitude = userAptitudeRepository.findByMember(member)
			.orElse(null); // 없으면 null

		if (userAptitude == null) {
			return new UserAptitudeResponseDto(AptitudeType.PENDING);
		}

		return new UserAptitudeResponseDto(userAptitude.getAptitudeType());
	}

	/**
	 * 마이페이지 - 내 적성 수정
	 */
	@Transactional
	public UserAptitudeResponseDto updateMyAptitude(Long memberId, AptitudeType newAptitudeType) {
		Member member = memberRepository.findActiveByIdOrThrow(memberId);

		// 1. 온보딩 완료 여부 확인 (안전장치)
		checkOnboardingCompleted(member);

		// 2. UserAptitude 조회, 없으면 새로 생성
		UserAptitude userAptitude = userAptitudeRepository.findByMember(member)
			.orElseGet(() -> {
				log.info("기존 적성 정보가 없어 새로 생성합니다. memberId: {}", memberId);
				return UserAptitude.builder()
					.member(member)
					.aptitudeType(AptitudeType.PENDING)
					.aptitudeTestCount(0)
					.build();
			});

		// 3. 새로운 적성으로 수동 업데이트
		userAptitude.manuallyUpdateAptitude(newAptitudeType);
		userAptitudeRepository.save(userAptitude);

		log.info("사용자 적성 수동 업데이트 완료. memberId: {}, newAptitude: {}", memberId, newAptitudeType);

		return new UserAptitudeResponseDto(userAptitude.getAptitudeType());
	}

	/**
	 * 온보딩 완료 여부를 확인하는 private 헬퍼 메서드
	 */
	private void checkOnboardingCompleted(Member member) {
		OnboardingProgress progress = onboardingProgressRepository.findByMember(member)
			.orElseThrow(() -> new CustomException(ErrorCode.ONBOARDING_NOT_STARTED));

		if (!progress.isCompleted()) {
			throw new CustomException(ErrorCode.ONBOARDING_NOT_COMPLETED);
		}
	}
}
