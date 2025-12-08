package com.salayo.locallifebackend.domain.onboarding.service;

import com.salayo.locallifebackend.domain.ai.aptitude.entity.UserAptitude;
import com.salayo.locallifebackend.domain.ai.aptitude.enums.AptitudeType;
import com.salayo.locallifebackend.domain.ai.aptitude.repository.UserAptitudeRepository;
import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.member.repository.MemberRepository;
import com.salayo.locallifebackend.domain.onboarding.dto.OnboardingAptitudeCheckRequestDto;
import com.salayo.locallifebackend.domain.onboarding.dto.OnboardingProgressResponseDto;
import com.salayo.locallifebackend.domain.onboarding.dto.OnboardingRegionRequestDto;
import com.salayo.locallifebackend.domain.onboarding.dto.OnboardingStatusResponseDto;
import com.salayo.locallifebackend.domain.onboarding.entity.OnboardingProgress;
import com.salayo.locallifebackend.domain.onboarding.enums.OnboardingStep;
import com.salayo.locallifebackend.domain.onboarding.enums.RegionType;
import com.salayo.locallifebackend.domain.onboarding.repository.OnboardingProgressRepository;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
public class OnboardingService {

	private final OnboardingProgressRepository onboardingProgressRepository;
	private final MemberRepository memberRepository;
	private final UserAptitudeRepository userAptitudeRepository;

	public OnboardingService(OnboardingProgressRepository onboardingProgressRepository,
		MemberRepository memberRepository, UserAptitudeRepository userAptitudeRepository) {
		this.onboardingProgressRepository = onboardingProgressRepository;
		this.memberRepository = memberRepository;
		this.userAptitudeRepository = userAptitudeRepository;
	}

	/**
	 * 온보딩 시작
	 */
	@Transactional
	public OnboardingProgressResponseDto startOnboarding(Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

		OnboardingProgress progress = onboardingProgressRepository.findByMember(member)
			.orElseGet(() -> {

				String sessionId = generateSessionId(memberId);
				OnboardingProgress newProgress = OnboardingProgress.builder()
					.member(member)
					.isCompleted(false)
					.sessionId(sessionId)
					.build();
				return onboardingProgressRepository.save(newProgress);
			});

		if (progress.isCompleted()) {
			log.info("온보딩 이미 완료 - memberId: {}", memberId);
			return buildCompletedResponse(member, progress);
		}

		if (progress.getCurrentStep() == OnboardingStep.MEMBER_INFO) {
			progress.moveToNextStep(false);
			log.info("온보딩 단계 진행: MEMBER_INFO -> REGION_SELECT");
		}

		log.info("온보딩 시작 - memberId: {}, sessionId: {}, currentStep: {}",
			memberId, progress.getSessionId(), progress.getCurrentStep());

		return buildProgressResponse(member, progress);
	}

	private String generateSessionId(Long memberId) {

		return String.format("ONB-%d-%s", memberId, UUID.randomUUID().toString().substring(0, 8));
	}

	/**
	 * 현재 온보딩 단계의 상세 정보 조회
	 */
	@Transactional(readOnly = true)
	public OnboardingProgressResponseDto getCurrentProgress(Long memberId) {
		Member member = memberRepository.findActiveByIdOrThrow(memberId);
		OnboardingProgress progress = getOnboardingProgress(member);

		AptitudeType aptitudeType = userAptitudeRepository.findByMember(member)
			.map(UserAptitude::getAptitudeType)
			.orElse(AptitudeType.PENDING);

		return OnboardingProgressResponseDto.builder()
			.sessionId(progress.getSessionId())
			.currentStep(progress.getCurrentStep())
			.isCompleted(progress.isCompleted())
			.regionType(progress.getRegionType())
			.aptitudeType(aptitudeType)
			.knowsAptitude(progress.getKnowsAptitude())
			.build();
	}

	/**
	 * 온보딩 진행 상태 조회 (내부 사용)
	 */
	private OnboardingProgress getOnboardingProgress(Member member) {

		return onboardingProgressRepository.findByMember(member)
			.orElseThrow(() -> {
				log.error("온보딩 진행 정보 없음 - memberId: {}", member.getId());
				return new CustomException(ErrorCode.ONBOARDING_NOT_STARTED);
			});
	}

	/**
	 * 온보딩 이어하기(Resume) 여부 판단을 위한 진행 상태 조회
	 */
	@Transactional(readOnly = true)
	public OnboardingStatusResponseDto getOnboardingStatus(Long memberId) {
		Member member = memberRepository.findActiveByIdOrThrow(memberId);

		AptitudeType aptitudeType = userAptitudeRepository.findByMember(member)
			.map(UserAptitude::getAptitudeType)
			.orElse(null);

		return onboardingProgressRepository.findByMember(member)
			.filter(progress -> !progress.isCompleted())
			.map(progress -> OnboardingStatusResponseDto.builder()
				.isCompleted(false)
				.currentStep(progress.getCurrentStep())
				.sessionId(progress.getSessionId())
				.aptitudeType(aptitudeType)
				.build())
			.orElseGet(() -> OnboardingStatusResponseDto.builder()
				.isCompleted(true)
				.aptitudeType(aptitudeType)
				.build());
	}

	/**
	 * 선호 지역 특징 선택 및 자동 지역 매핑
	 */
	@Transactional
	public OnboardingProgressResponseDto selectRegion(Long memberId, OnboardingRegionRequestDto regionRequestDto) {
		Member member = memberRepository.findActiveByIdOrThrow(memberId);
		OnboardingProgress progress = getOnboardingProgress(member);

		if (progress.getCurrentStep() != OnboardingStep.REGION_SELECT) {
			log.error("잘못된 온보딩 단계 - 현재: {}, 예상: REGION_SELECT", progress.getCurrentStep());
			throw new CustomException(ErrorCode.INVALID_ONBOARDING_STEP);
		}

		RegionType regionType = regionRequestDto.regionType();
		progress.updateRegion(regionType);
		progress.moveToNextStep(false);

		log.info("선호 지역 특징 선택 완료 - memberId: {}, regionType: {}",
			memberId, regionType);

		return OnboardingProgressResponseDto.builder()
			.sessionId(progress.getSessionId())
			.currentStep(OnboardingStep.REGION_SELECT)
			.nextStep(OnboardingStep.APTITUDE_CHECK)
			.isCompleted(false)
			.regionType(regionType)
			.guideMessage("선호 지역 특징이 설정되었습니다. 적성을 알고 계신가요?")
			.nextAction(OnboardingProgressResponseDto.NextAction.builder()
				.type(OnboardingStep.APTITUDE_CHECK)
				.endpoint("/onboarding/aptitude-check")
				.method("POST")
				.description("적성 인지 여부 확인")
				.build())
			.build();
	}

	/**
	 * 적성 인지 여부 확인
	 */
	@Transactional
	public OnboardingProgressResponseDto checkAptitudeKnowledge(Long memberId,
		OnboardingAptitudeCheckRequestDto aptitudeCheckRequestDto) {
		Member member = memberRepository.findActiveByIdOrThrow(memberId);
		OnboardingProgress progress = getOnboardingProgress(member);

		if (progress.getCurrentStep() != OnboardingStep.APTITUDE_CHECK) {
			throw new CustomException(ErrorCode.INVALID_ONBOARDING_STEP);
		}

		boolean knowsAptitude = aptitudeCheckRequestDto.knowsAptitude();
		progress.updateKnowsAptitude(knowsAptitude);
		progress.moveToNextStep(knowsAptitude);

		log.info("적성 인지 여부 확인 - memberId: {}, knowsAptitude: {}, nextStep: {}",
			memberId, knowsAptitude, progress.getCurrentStep());

		if (knowsAptitude) {
			return OnboardingProgressResponseDto.builder()
				.sessionId(progress.getSessionId())
				.currentStep(OnboardingStep.APTITUDE_CHECK)
				.nextStep(OnboardingStep.APTITUDE_MANUAL)
				.isCompleted(false)
				.regionType(progress.getRegionType())
				.knowsAptitude(true)
				.guideMessage("적성을 선택해주세요.")
				.nextAction(OnboardingProgressResponseDto.NextAction.builder()
					.type(OnboardingStep.APTITUDE_MANUAL)
					.endpoint("/ai/aptitude/select")
					.method("POST")
					.description("수동 적성 선택")
					.build())
				.build();
		} else {
			return OnboardingProgressResponseDto.builder()
				.sessionId(progress.getSessionId())
				.currentStep(OnboardingStep.APTITUDE_CHECK)
				.nextStep(OnboardingStep.APTITUDE_AI_TEST)
				.isCompleted(false)
				.regionType(progress.getRegionType())
				.knowsAptitude(false)
				.guideMessage("AI 적성 검사를 시작합니다.")
				.nextAction(OnboardingProgressResponseDto.NextAction.builder()
					.type(OnboardingStep.APTITUDE_AI_TEST)
					.endpoint("/ai/aptitude/test/start")
					.method("POST")
					.description("AI 적성 검사 시작")
					.build())
				.build();
		}
	}

	/**
	 * 온보딩 완료 처리 (적성 선택 또는 AI 검사 완료 후 호출)
	 */
	@Transactional
	public OnboardingProgressResponseDto completeOnboarding(Long memberId) {
		Member member = memberRepository.findActiveByIdOrThrow(memberId);
		OnboardingProgress progress = getOnboardingProgress(member);

		if (progress.isCompleted()) {
			log.info("온보딩 이미 완료 (idempotent) - memberId: {}", memberId);
			return buildCompletedResponse(member, progress);
		}

		if (progress.getCurrentStep() != OnboardingStep.APTITUDE_MANUAL &&
			progress.getCurrentStep() != OnboardingStep.APTITUDE_AI_TEST) {
			throw new CustomException(ErrorCode.INVALID_ONBOARDING_STEP);
		}

		UserAptitude userAptitude = userAptitudeRepository.findByMember(member)
			.orElseThrow(() -> new CustomException(ErrorCode.APTITUDE_NOT_FOUND));

		progress.completeOnboardingStep();
		log.info("온보딩 완료 - memberId: {}, regionType: {}, aptitudeType: {}",
			memberId, progress.getRegionType(), userAptitude.getAptitudeType());

		return OnboardingProgressResponseDto.builder()
			.sessionId(progress.getSessionId())
			.currentStep(OnboardingStep.COMPLETED)
			.nextStep(null)
			.isCompleted(true)
			.regionType(progress.getRegionType())
			.aptitudeType(userAptitude.getAptitudeType())
			.knowsAptitude(progress.getKnowsAptitude())
			.guideMessage("온보딩이 완료되었습니다!")
			.nextAction(OnboardingProgressResponseDto.NextAction.builder()
				.type(OnboardingStep.COMPLETED)
				.endpoint("/home")
				.method("GET")
				.description("홈 화면으로 이동")
				.build())
			.build();
	}

	/**
	 * 완료된 온보딩에 대한 응답 DTO 생성
	 */
	private OnboardingProgressResponseDto buildCompletedResponse(Member member, OnboardingProgress progress) {
		AptitudeType aptitudeType = userAptitudeRepository.findByMember(member)
			.map(UserAptitude::getAptitudeType)
			.orElse(AptitudeType.PENDING);

		return OnboardingProgressResponseDto.builder()
			.sessionId(progress.getSessionId())
			.currentStep(OnboardingStep.COMPLETED)
			.nextStep(null)
			.isCompleted(true)
			.regionType(progress.getRegionType())
			.aptitudeType(aptitudeType)
			.knowsAptitude(progress.getKnowsAptitude())
			.guideMessage("온보딩이 완료되었습니다!")
			.nextAction(OnboardingProgressResponseDto.NextAction.builder()
				.type(OnboardingStep.COMPLETED)
				.endpoint("/home")
				.method("GET")
				.description("홈 화면으로 이동")
				.build())
			.build();
	}

	/**
	 * 진행 중인 온보딩에 대한 응답 DTO 생성
	 */
	private OnboardingProgressResponseDto buildProgressResponse(Member member, OnboardingProgress progress) {
		AptitudeType aptitudeType = userAptitudeRepository.findByMember(member)
			.map(UserAptitude::getAptitudeType)
			.orElse(AptitudeType.PENDING);

		OnboardingStep current = progress.getCurrentStep();
		boolean knows = progress.getKnowsAptitude() != null && progress.getKnowsAptitude();
		OnboardingStep next = current.getNextStep(knows);

		return OnboardingProgressResponseDto.builder()
			.sessionId(progress.getSessionId())
			.currentStep(current)
			.nextStep(next)
			.isCompleted(false)
			.regionType(progress.getRegionType())
			.aptitudeType(aptitudeType)
			.knowsAptitude(progress.getKnowsAptitude())
			.guideMessage(buildGuideMessage(current))
			.nextAction(buildNextAction(current))
			.build();
	}

	private String buildGuideMessage(OnboardingStep step) {
		return switch (step) {
			case MEMBER_INFO, REGION_SELECT -> "온보딩을 시작합니다. 선호 지역을 선택해주세요.";
			case APTITUDE_CHECK -> "선호 지역 특징이 설정되었습니다. 적성을 알고 계신가요?";
			case APTITUDE_MANUAL -> "적성을 선택해주세요.";
			case APTITUDE_AI_TEST -> "AI 적성 검사를 시작합니다.";
			case COMPLETED -> "온보딩이 완료되었습니다!";
		};
	}

	private OnboardingProgressResponseDto.NextAction buildNextAction(OnboardingStep current) {
		return switch (current) {
			case MEMBER_INFO, REGION_SELECT -> OnboardingProgressResponseDto.NextAction.builder()
				.type(OnboardingStep.REGION_SELECT)
				.endpoint("/onboarding/region")
				.method("POST")
				.description("선호 지역 특징 선택")
				.build();
			case APTITUDE_CHECK -> OnboardingProgressResponseDto.NextAction.builder()
				.type(OnboardingStep.APTITUDE_CHECK)
				.endpoint("/onboarding/aptitude-check")
				.method("POST")
				.description("적성 인지 여부 확인")
				.build();
			case APTITUDE_MANUAL -> OnboardingProgressResponseDto.NextAction.builder()
				.type(OnboardingStep.APTITUDE_MANUAL)
				.endpoint("/ai/aptitude/select")
				.method("POST")
				.description("수동 적성 선택")
				.build();
			case APTITUDE_AI_TEST -> OnboardingProgressResponseDto.NextAction.builder()
				.type(OnboardingStep.APTITUDE_AI_TEST)
				.endpoint("/ai/aptitude/test/start")
				.method("POST")
				.description("AI 적성 검사 시작")
				.build();
			case COMPLETED -> OnboardingProgressResponseDto.NextAction.builder()
				.type(OnboardingStep.COMPLETED)
				.endpoint("/home")
				.method("GET")
				.description("홈 화면으로 이동")
				.build();
		};
	}

	/**
	 * [내부용] 온보딩 완료 여부 검증 완료되지 않았을 경우 예외를 발생 시킴
	 */
	@Transactional(readOnly = true)
	public OnboardingProgress getCompletedOnboardingProgress(Long memberId) {
		Member member = memberRepository.findActiveByIdOrThrow(memberId);
		OnboardingProgress progress = onboardingProgressRepository.findByMember(member)
			.orElseThrow(() -> new CustomException(ErrorCode.ONBOARDING_NOT_STARTED));

		if (!progress.isCompleted()) {
			throw new CustomException(ErrorCode.ONBOARDING_NOT_COMPLETED);
		}
		return progress;
	}

	/**
	 * [내부용] 마이페이지에서 선호 지역 변경 시, 온보딩 상태를 확인하고 RegionType을 변경
	 *
	 * @return 동일한 RegionType을 선택했는지 여부
	 */
	@Transactional
	public boolean checkAndUpdateRegionTypeForMypage(Long memberId, RegionType newRegionType) {

		OnboardingProgress progress = getCompletedOnboardingProgress(memberId);

		if (progress.getRegionType() == newRegionType) {
			log.info("동일한 지역 특징 선택 - memberId: {}, regionType: {}", memberId, newRegionType);
			return true;
		}

		progress.updateRegion(newRegionType);

		return false;
	}

	/**
	 * [내부용] 적성 검사 완료 후 온보딩 상태를 완료로 변경
	 * AptitudeService에서 호출
	 */
	@Transactional
	public void completeOnboardingAfterAptitudeTest(Long memberId) {
		Member member = memberRepository.findActiveByIdOrThrow(memberId);
		OnboardingProgress progress = getOnboardingProgress(member);

		if (progress.isCompleted()) {
			return;
		}

		progress.completeOnboardingStep();
		log.info("적성 검사 완료에 따른 온보딩 상태 업데이트 - memberId: {}", memberId);
	}
}
