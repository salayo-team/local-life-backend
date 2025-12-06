package com.salayo.locallifebackend.domain.ai.aptitude.service;

import com.salayo.locallifebackend.domain.ai.aptitude.dto.AptitudeQuestionResponseDto;
import com.salayo.locallifebackend.domain.ai.aptitude.dto.AptitudeResumeResponseDto;
import com.salayo.locallifebackend.domain.ai.aptitude.dto.AptitudeTestStartResponseDto;
import com.salayo.locallifebackend.domain.ai.aptitude.dto.CanRetakeTestResponseDto;
import com.salayo.locallifebackend.domain.ai.aptitude.entity.AptitudeTestHistory;
import com.salayo.locallifebackend.domain.ai.aptitude.entity.AptitudeTestProgress;
import com.salayo.locallifebackend.domain.ai.aptitude.entity.UserAptitude;
import com.salayo.locallifebackend.domain.ai.aptitude.enums.AptitudeType;
import com.salayo.locallifebackend.domain.ai.aptitude.repository.AptitudeTestProgressRepository;
import com.salayo.locallifebackend.domain.ai.aptitude.repository.UserAptitudeRepository;
import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.member.repository.MemberRepository;
import com.salayo.locallifebackend.domain.onboarding.entity.OnboardingProgress;
import com.salayo.locallifebackend.domain.onboarding.repository.OnboardingProgressRepository;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import com.salayo.locallifebackend.global.util.CacheKeyPrefix;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AptitudeTestProgressService {

	private final UserAptitudeRepository userAptitudeRepository;
	private final AptitudeTestProgressRepository aptitudeTestProgressRepository;
	private final OnboardingProgressRepository onboardingProgressRepository;
	private final MemberRepository memberRepository;
	private final AptitudeTestHistoryService testHistoryService;
	private final AptitudeAiService aptitudeAiService;
	private final AptitudeCacheService aptitudeCacheService;

	public AptitudeTestProgressService(UserAptitudeRepository userAptitudeRepository,
			AptitudeTestProgressRepository aptitudeTestProgressRepository,
			OnboardingProgressRepository onboardingProgressRepository,
			MemberRepository memberRepository,
			AptitudeTestHistoryService testHistoryService,
			AptitudeAiService aptitudeAiService,
			AptitudeCacheService aptitudeCacheService) {
			this.userAptitudeRepository = userAptitudeRepository;
			this.aptitudeTestProgressRepository = aptitudeTestProgressRepository;
			this.onboardingProgressRepository = onboardingProgressRepository;
			this.memberRepository = memberRepository;
			this.testHistoryService = testHistoryService;
			this.aptitudeAiService = aptitudeAiService;
			this.aptitudeCacheService = aptitudeCacheService;
	}

	@Transactional
	public AptitudeTestStartResponseDto startTest(Long memberId) {
		Member member = memberRepository.findActiveByIdOrThrow(memberId);

		UserAptitude userAptitude = userAptitudeRepository.findByMember(member)
			.orElseGet(() -> {
				log.info("새로운 사용자의 UserAptitude을 생성합니다. memberId: {}", memberId);

				return userAptitudeRepository.save(
					UserAptitude.builder()
						.member(member)
						.aptitudeType(AptitudeType.PENDING)
						.aptitudeTestCount(0)
						.build()
				);
			});

		AptitudeTestProgress progress = aptitudeTestProgressRepository.findByUserAptitude(userAptitude)
			.orElseGet(() -> {
				log.info("새로운 사용자의 AptitudeTestProgress를 생성합니다. memberId: {}", memberId);

				AptitudeTestProgress newProgress = AptitudeTestProgress.builder()
					.userAptitude(userAptitude)
					.aptitudeTestStep(0)
					.mypageTestCount(0)
					.build();

				return aptitudeTestProgressRepository.save(newProgress);
			});

		// OnboardingProgress가 존재하고 완료되었다면 true, 그렇지 않으면 false
		boolean isRetakeAfterOnboarding = onboardingProgressRepository.findByMember(member)
			.map(OnboardingProgress::isCompleted) // Optional<OnboardingProgress> -> Optional<Boolean>
			.orElse(false); // Optional<Boolean> -> boolean (값이 없으면 false)

		if (isRetakeAfterOnboarding) {
			log.info("온보딩 완료 사용자입니다. 마이페이지에서 테스트 횟수를 검사합니다. count: {}", progress.getMypageTestCount());

			if (progress.getMypageTestCount() >= CacheKeyPrefix.APTITUDE_MAX_TEST_COUNT) {
				throw new CustomException(ErrorCode.APTITUDE_TEST_LIMIT_EXCEEDED);
			}
			progress.incrementMypageTestCount();
		} else {
			log.info("온보딩 진행 중인 사용자입니다. 횟수 제한 검사를 건너뜁니다.");
		}

		// 세션 ID 생성
		String sessionId = generateSessionId(memberId);

		// 미완료된 테스트 이력만 삭제 (완료된 이력은 보존)
		testHistoryService.deleteIncompleteTests(member);

		// Redis 기존 데이터 정리 (중복 방지)
		aptitudeCacheService.deleteAllTestData(memberId);

		// Redis에 테스트 진행 상태 저장
		aptitudeCacheService.saveTestProgress(memberId, "1");

		// Redis에 세션 ID 저장
		aptitudeCacheService.saveSessionId(memberId, sessionId);

		// 첫 질문 가져오기 - AI가 동적으로 생성
		AptitudeQuestionResponseDto firstQuestion = aptitudeAiService.getNextQuestion(memberId, 1);

		// 첫 질문을 Redis에 저장
		aptitudeCacheService.saveQuestion(memberId, 1, firstQuestion.getQuestion());

		return new AptitudeTestStartResponseDto(1, CacheKeyPrefix.APTITUDE_TOTAL_QUESTIONS, firstQuestion);
	}

	// 세션 ID 생성
	private String generateSessionId(Long memberId) {
		return String.format("APT-%d-%d", memberId, System.currentTimeMillis());
	}

	public String getOrCreateSessionId(Long memberId) {
		String sessionId = aptitudeCacheService.getSessionId(memberId);
		if (sessionId == null) {

			// 세션이 만료되거나 없는 경우 재생성
			sessionId = generateSessionId(memberId);
			aptitudeCacheService.saveSessionId(memberId, sessionId);
			log.warn("세션이 없어 재생성 - memberId: {}, newSessionId: {}", memberId, sessionId);
		} else {

			// 세션 TTL 갱신 (60분 연장)
			aptitudeCacheService.refreshSession(memberId);
		}
		return sessionId;
	}

	@Transactional(readOnly = true)
	public CanRetakeTestResponseDto canRetakeTest(Long memberId) {
		Member member = memberRepository.findActiveByIdOrThrow(memberId);

		boolean isOnboardingCompleted = onboardingProgressRepository.findByMember(member)
			.map(OnboardingProgress::isCompleted)
			.orElse(false);

		// 온보딩 미완료 시 항상 재검사 가능
		if (!isOnboardingCompleted) {
			return new CanRetakeTestResponseDto(true, 0, CacheKeyPrefix.APTITUDE_MAX_TEST_COUNT);
		}

		// 온보딩 완료 후에는 DB에 저장된 재검사 횟수를 기준으로 판단
		return aptitudeTestProgressRepository.findByMember(member)
			.map(progress -> new CanRetakeTestResponseDto(
				progress.getMypageTestCount() < CacheKeyPrefix.APTITUDE_MAX_TEST_COUNT,
				progress.getMypageTestCount(),
				CacheKeyPrefix.APTITUDE_MAX_TEST_COUNT
			))
			.orElse(new CanRetakeTestResponseDto(true, 0,
				CacheKeyPrefix.APTITUDE_MAX_TEST_COUNT)); // progress 정보가 없으면 첫 재검사로 간주
	}

	// 이어하기 기능
	@Transactional
	public AptitudeResumeResponseDto resumeTest(Long memberId) {
		Member member = memberRepository.findActiveByIdOrThrow(memberId);

		AptitudeTestProgress progress = aptitudeTestProgressRepository.findByMember(member)
			.orElseThrow(() -> new CustomException(ErrorCode.NO_INCOMPLETE_TEST));

		if (progress.getPartialSessionId() != null) {
			String sessionId = progress.getPartialSessionId();
			Integer lastStep = progress.getLastPartialStep();

			// 세션의 이력 조회
			List<AptitudeTestHistory> histories = testHistoryService
				.getSessionHistories(sessionId, member);

			if (!histories.isEmpty()) {
				// Redis 복구 - 세션 ID와 진행 상태
				aptitudeCacheService.saveSessionId(memberId, sessionId);
				aptitudeCacheService.saveTestProgress(memberId, String.valueOf(lastStep + 1));

				// 기존 점수 복구
				for (AptitudeTestHistory history : histories) {
					if (history.getAnalyzedAptitudeType() != null) {
						int score = history.getConfidenceScore() > 0.7 ? 2 : 1;
						aptitudeCacheService.updateAptitudeScore(memberId,
							history.getAnalyzedAptitudeType(), score);
					}

					// 답변도 복구
					aptitudeCacheService.saveAnswer(memberId, history.getStep(),
						history.getUserResponse());
				}

				// 다음 질문 반환 - AI가 대화 맥락을 고려하여 생성
				AptitudeQuestionResponseDto nextQuestion = aptitudeAiService.getNextQuestion(
					member.getId(),
					lastStep + 1
				);

				return AptitudeResumeResponseDto.builder()
					.sessionId(sessionId)
					.nextStep(lastStep + 1)
					.totalSteps(CacheKeyPrefix.APTITUDE_TOTAL_QUESTIONS)
					.nextQuestion(nextQuestion)
					.completedSteps(lastStep)
					.resumeMessage(String.format("%d단계부터 이어서 진행합니다.", lastStep + 1))
					.build();
			}
		}

		throw new CustomException(ErrorCode.NO_INCOMPLETE_TEST);
	}
}

