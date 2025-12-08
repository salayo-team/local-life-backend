package com.salayo.locallifebackend.domain.ai.aptitude.service;

import com.salayo.locallifebackend.domain.ai.aptitude.dto.AptitudeAnswerRequestDto;
import com.salayo.locallifebackend.domain.ai.aptitude.dto.AptitudeQuestionResponseDto;
import com.salayo.locallifebackend.domain.ai.aptitude.dto.AptitudeTextProgressResponseDto;
import com.salayo.locallifebackend.domain.ai.aptitude.dto.AptitudeTestResultResponseDto;
import com.salayo.locallifebackend.domain.ai.aptitude.entity.AptitudeTestHistory;
import com.salayo.locallifebackend.domain.ai.aptitude.entity.AptitudeTestProgress;
import com.salayo.locallifebackend.domain.ai.aptitude.entity.UserAptitude;
import com.salayo.locallifebackend.domain.ai.aptitude.enums.AptitudeType;
import com.salayo.locallifebackend.domain.ai.aptitude.repository.AptitudeTestProgressRepository;
import com.salayo.locallifebackend.domain.ai.aptitude.repository.UserAptitudeRepository;
import com.salayo.locallifebackend.domain.ai.aptitude.dto.AiAptitudeAnalysisResponseDto;
import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.member.repository.MemberRepository;
import com.salayo.locallifebackend.domain.onboarding.service.OnboardingService;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import com.salayo.locallifebackend.global.util.CacheKeyPrefix;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AptitudeService {

	private final UserAptitudeRepository userAptitudeRepository;
	private final AptitudeTestProgressRepository aptitudeTestProgressRepository;
	private final MemberRepository memberRepository;
	private final AptitudeTestProgressService aptitudeTestProgressService;
	private final AptitudeTestHistoryService testHistoryService;
	private final AptitudeAiService aptitudeAiService;
	private final AptitudeCacheService aptitudeCacheService;
	private final OnboardingService onboardingService;

	public AptitudeService(UserAptitudeRepository userAptitudeRepository,
		AptitudeTestProgressRepository aptitudeTestProgressRepository, MemberRepository memberRepository,
		AptitudeTestProgressService aptitudeTestProgressService,
		AptitudeTestHistoryService testHistoryService,
		AptitudeAiService aptitudeAiService,
		AptitudeCacheService aptitudeCacheService, OnboardingService onboardingService) {
		this.userAptitudeRepository = userAptitudeRepository;
		this.aptitudeTestProgressRepository = aptitudeTestProgressRepository;
		this.memberRepository = memberRepository;
		this.aptitudeTestProgressService = aptitudeTestProgressService;
		this.testHistoryService = testHistoryService;
		this.aptitudeAiService = aptitudeAiService;
		this.aptitudeCacheService = aptitudeCacheService;
		this.onboardingService = onboardingService;
	}

	@Transactional
	public AptitudeTextProgressResponseDto submitAnswer(Long memberId, AptitudeAnswerRequestDto aptitudeAnswerRequestDto) {
		Member member = memberRepository.findActiveByIdOrThrow(memberId);

		String currentStepStr = aptitudeCacheService.getTestProgress(memberId);
		if (currentStepStr == null) {
			throw new CustomException(ErrorCode.NOT_FOUND_TEST_PROGRESS);
		}

		String sessionId = aptitudeTestProgressService.getOrCreateSessionId(memberId);

		AiAptitudeAnalysisResponseDto aiAnalysis = aptitudeAiService.analyzeResponse(
			aptitudeAnswerRequestDto.getAnswer(),
			member.getId(),
			aptitudeAnswerRequestDto.getStep(),
			aptitudeAnswerRequestDto.getQuestionText()
		);

		aptitudeCacheService.saveAnswer(memberId, aptitudeAnswerRequestDto.getStep(),
			aptitudeAnswerRequestDto.getAnswer());

		log.info("[TEST] Redis 답변 저장 완료 - memberId: {}, step: {}", memberId, aptitudeAnswerRequestDto.getStep());

		updateScoreFromAnalysis(memberId, aiAnalysis);

		log.info("[JSON 파싱 테스트] Step {} 처리 완료 - 적성: {}, 신뢰도: {}",
			aptitudeAnswerRequestDto.getStep(),
			aiAnalysis.getAptitudeType(),
			aiAnalysis.getConfidenceScore());

		String aiResponseStr;
		if (aiAnalysis.getAptitudeType() != null) {
			aiResponseStr = String.format("%s - %s (신뢰도: %.2f)",
				aiAnalysis.getAptitudeType(),
				aiAnalysis.getReason(),
				aiAnalysis.getConfidenceScore());
		} else {
			aiResponseStr = String.format("무효 답변 - %s",
				aiAnalysis.getReason() != null ? aiAnalysis.getReason() : "적성 판단 불가");
		}

		Map<AptitudeType, Integer> currentScores = aptitudeCacheService.getAllAptitudeScores(memberId);
		log.info("[TEST] 현재 적성 점수 상태: {}", currentScores);

		AptitudeTestHistory history = AptitudeTestHistory.builder()
			.member(member)
			.step(aptitudeAnswerRequestDto.getStep())
			.questionText(aptitudeAnswerRequestDto.getQuestionText())
			.userResponse(aptitudeAnswerRequestDto.getAnswer())
			.aiResponse(aiResponseStr)
			.analyzedAptitudeType(parseAptitudeType(aiAnalysis.getAptitudeType()))
			.confidenceScore(aiAnalysis.getConfidenceScore())
			.sessionId(sessionId)
			.isCompleted(false)
			.build();

		testHistoryService.saveTestHistory(member, history);

		savePartialResult(member, sessionId, aptitudeAnswerRequestDto.getStep());

		if (aptitudeAnswerRequestDto.getStep() >= CacheKeyPrefix.APTITUDE_TOTAL_QUESTIONS) {
			aptitudeCacheService.deleteTestProgress(memberId);

			return calculateAndSaveResult(member);
		} else {
			aptitudeCacheService.updateTestProgress(memberId, aptitudeAnswerRequestDto.getStep() + 1);

			AptitudeQuestionResponseDto nextQuestion = aptitudeAiService.getNextQuestion(
				member.getId(),
				aptitudeAnswerRequestDto.getStep() + 1
			);
			aptitudeCacheService.saveQuestion(memberId, aptitudeAnswerRequestDto.getStep() + 1,
				nextQuestion.getQuestion());

			return new AptitudeTextProgressResponseDto(
				aptitudeAnswerRequestDto.getStep() + 1,
				CacheKeyPrefix.APTITUDE_TOTAL_QUESTIONS,
				nextQuestion,
				false,
				null
			);
		}
	}

	private void updateScoreFromAnalysis(Long memberId, AiAptitudeAnalysisResponseDto aiAnalysis) {

		if (aiAnalysis.getAptitudeType() == null) {
			log.warn("AI 분석 결과 적성 타입이 NULL - 무효 답변으로 처리");

			return;
		}

		try {
			AptitudeType aptitudeType = AptitudeType.valueOf(aiAnalysis.getAptitudeType().toUpperCase());

			/**
			 * 신뢰도에 따른 가중치 적용 (높은 신뢰도일수록 더 높은 점수)
			 */
			int score = aiAnalysis.getConfidenceScore() > 0.7 ? 2 : 1;

			log.info("[가중치 테스트] 신뢰도 {}에 따른 점수: {} (기준: 0.7 초과 시 2점, 이하 시 1점)",
				aiAnalysis.getConfidenceScore(), score);

			aptitudeCacheService.updateAptitudeScore(memberId, aptitudeType, score);
			log.debug("적성 점수 업데이트 - memberId: {}, type: {}, score: {}, confidence: {}",
				memberId, aptitudeType, score, aiAnalysis.getConfidenceScore());
		} catch (IllegalArgumentException e) {
			log.warn("AI 응답에서 유효하지 않은 적성 타입: {}", aiAnalysis.getAptitudeType());

			for (AptitudeType type : AptitudeType.values()) {

				if (aiAnalysis.getReason() != null &&
					(aiAnalysis.getReason().contains(type.name()) ||
						aiAnalysis.getReason().contains(type.getTitle()))) {

					aptitudeCacheService.updateAptitudeScore(memberId, type, 1);
					log.debug("Fallback 적성 점수 업데이트 - memberId: {}, type: {}", memberId, type);

					break;
				}
			}
		}
	}

	private AptitudeType parseAptitudeType(String aptitudeTypeStr) {
		try {
			return AptitudeType.valueOf(aptitudeTypeStr.toUpperCase());
		} catch (IllegalArgumentException | NullPointerException e) {
			log.warn("유효하지 않은 적성 타입 문자열: {}, 기본값 NATURE 반환", aptitudeTypeStr);
			return AptitudeType.NATURE;
		}
	}

	private void savePartialResult(Member member, String sessionId, int currentStep) {

		if (currentStep >= CacheKeyPrefix.APTITUDE_TOTAL_QUESTIONS) {
			return;
		}

		AptitudeTestProgress progress = aptitudeTestProgressRepository.findByMember(member)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_TEST_PROGRESS));

		progress.updatePartialProgress(currentStep, sessionId);

		log.info("[부분 저장] memberId: {}, step: {}, sessionId: {}", member.getId(), currentStep, sessionId);
	}

	private AptitudeTextProgressResponseDto calculateAndSaveResult(Member member) {
		Map<AptitudeType, Integer> scores = aptitudeCacheService.getAllAptitudeScores(member.getId());

		AptitudeType finalAptitude = aptitudeAiService.calculateFinalAptitude(scores);

		UserAptitude userAptitude = userAptitudeRepository.findByMember(member)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_APTITUDE_NOT_FOUND));

		userAptitude.updateAptitudeFromTest(finalAptitude);

		aptitudeTestProgressRepository.findByMember(member)
			.ifPresent(AptitudeTestProgress::clearPartialProgress);

		onboardingService.completeOnboardingAfterAptitudeTest(member.getId());

		String sessionId = aptitudeCacheService.getSessionId(member.getId());

		if (sessionId != null) {
			testHistoryService.markSessionAsCompleted(sessionId);
			log.info("[테스트 완료] 최종 적성: {}", finalAptitude);
		}

		log.info("[TEST] Redis 데이터 정리 시작 - memberId: {}", member.getId());
		aptitudeCacheService.deleteAllTestData(member.getId());
		log.info("[TEST] Redis 데이터 정리 완료 - memberId: {}", member.getId());

		return new AptitudeTextProgressResponseDto(
			CacheKeyPrefix.APTITUDE_TOTAL_QUESTIONS,
			CacheKeyPrefix.APTITUDE_TOTAL_QUESTIONS,
			null,
			true,
			finalAptitude
		);
	}

	@Transactional
	public AptitudeTestResultResponseDto selectAptitudeManually(Long memberId, AptitudeType aptitudeType) {

		Member member = memberRepository.findActiveByIdOrThrow(memberId);

		UserAptitude userAptitude = userAptitudeRepository.findByMember(member)
			.orElse(UserAptitude.builder()
				.member(member)
				.aptitudeType(aptitudeType.PENDING)
				.aptitudeTestCount(0)
				.build());

		userAptitude.updateAptitudeFromTest(aptitudeType);

		onboardingService.completeOnboardingAfterAptitudeTest(memberId);

		return new AptitudeTestResultResponseDto(aptitudeType);
	}
}
