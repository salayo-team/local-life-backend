package com.salayo.locallifebackend.domain.ai.aptitude.service;

import com.salayo.locallifebackend.domain.ai.aptitude.dto.AptitudeAnswerRequestDto;
import com.salayo.locallifebackend.domain.ai.aptitude.dto.AptitudeQuestionResponseDto;
import com.salayo.locallifebackend.domain.ai.aptitude.dto.AptitudeTextProgressResponseDto;
import com.salayo.locallifebackend.domain.ai.aptitude.dto.AptitudeTestResultResponseDto;
import com.salayo.locallifebackend.domain.ai.aptitude.dto.UserAptitudeResponseDto;
import com.salayo.locallifebackend.domain.ai.aptitude.entity.AptitudeTestHistory;
import com.salayo.locallifebackend.domain.ai.aptitude.entity.AptitudeTestProgress;
import com.salayo.locallifebackend.domain.ai.aptitude.entity.UserAptitude;
import com.salayo.locallifebackend.domain.ai.aptitude.enums.AptitudeType;
import com.salayo.locallifebackend.domain.ai.aptitude.repository.AptitudeTestProgressRepository;
import com.salayo.locallifebackend.domain.ai.aptitude.repository.UserAptitudeRepository;
import com.salayo.locallifebackend.domain.ai.aptitude.dto.AiAptitudeAnalysisResponseDto;
import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.member.repository.MemberRepository;
import com.salayo.locallifebackend.domain.onboarding.entity.OnboardingProgress;
import com.salayo.locallifebackend.domain.onboarding.repository.OnboardingProgressRepository;
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
	private final OnboardingProgressRepository onboardingProgressRepository;
	private final AptitudeTestProgressService aptitudeTestProgressService;
	private final AptitudeTestHistoryService testHistoryService;
	private final AptitudeAiService aptitudeAiService;
	private final AptitudeCacheService aptitudeCacheService;
	private final OnboardingService onboardingService;

	public AptitudeService(UserAptitudeRepository userAptitudeRepository,
		AptitudeTestProgressRepository aptitudeTestProgressRepository, MemberRepository memberRepository,
		OnboardingProgressRepository onboardingProgressRepository,
		AptitudeTestProgressService aptitudeTestProgressService,
		AptitudeTestHistoryService testHistoryService,
		AptitudeAiService aptitudeAiService,
		AptitudeCacheService aptitudeCacheService, OnboardingService onboardingService) {
		this.userAptitudeRepository = userAptitudeRepository;
		this.aptitudeTestProgressRepository = aptitudeTestProgressRepository;
		this.memberRepository = memberRepository;
		this.onboardingProgressRepository = onboardingProgressRepository;
		this.aptitudeTestProgressService = aptitudeTestProgressService;
		this.testHistoryService = testHistoryService;
		this.aptitudeAiService = aptitudeAiService;
		this.aptitudeCacheService = aptitudeCacheService;
		this.onboardingService = onboardingService;
	}

	@Transactional
	public AptitudeTextProgressResponseDto submitAnswer(Long memberId, AptitudeAnswerRequestDto aptitudeAnswerRequestDto) {
		Member member = memberRepository.findActiveByIdOrThrow(memberId);

		// Redis에서 현재 진행 상태 확인
		String currentStepStr = aptitudeCacheService.getTestProgress(memberId);
		if (currentStepStr == null) {
			throw new CustomException(ErrorCode.NOT_FOUND_TEST_PROGRESS);
		}

		// AptitudeTestProgressService에게 세션 ID 요청
		String sessionId = aptitudeTestProgressService.getOrCreateSessionId(memberId);

		// 답변 분석 (JSON 파싱된 객체 반환) - 대화 맥락 포함
		AiAptitudeAnalysisResponseDto aiAnalysis = aptitudeAiService.analyzeResponse(
			aptitudeAnswerRequestDto.getAnswer(),
			member.getId(),
			aptitudeAnswerRequestDto.getStep(),
			aptitudeAnswerRequestDto.getQuestionText()
		);

		// Redis에 답변 저장
		aptitudeCacheService.saveAnswer(memberId, aptitudeAnswerRequestDto.getStep(),
			aptitudeAnswerRequestDto.getAnswer());

		// 디버깅용 로그 (테스트 후 제거)
		log.info("[TEST] Redis 답변 저장 완료 - memberId: {}, step: {}", memberId, aptitudeAnswerRequestDto.getStep());

		// AI 분석 결과에서 적성 추출 및 점수 업데이트 (객체로 변경)
		updateScoreFromAnalysis(memberId, aiAnalysis);

		// JSON 파싱 테스트 로그
		log.info("[JSON 파싱 테스트] Step {} 처리 완료 - 적성: {}, 신뢰도: {}",
			aptitudeAnswerRequestDto.getStep(),
			aiAnalysis.getAptitudeType(),
			aiAnalysis.getConfidenceScore());

		// 이력 저장용 AI 응답 문자열 생성
		String aiResponseStr;
		if (aiAnalysis.getAptitudeType() != null) {
			aiResponseStr = String.format("%s - %s (신뢰도: %.2f)",
				aiAnalysis.getAptitudeType(),
				aiAnalysis.getReason(),
				aiAnalysis.getConfidenceScore());
		} else {
			// 무효 답변인 경우
			aiResponseStr = String.format("무효 답변 - %s",
				aiAnalysis.getReason() != null ? aiAnalysis.getReason() : "적성 판단 불가");
		}

		// 디버깅용 로그
		Map<AptitudeType, Integer> currentScores = aptitudeCacheService.getAllAptitudeScores(memberId);
		log.info("[TEST] 현재 적성 점수 상태: {}", currentScores);

		// 이력 저장 (AI 분석 결과를 포함하여 저장)
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

		// 모든 단계에서 부분 저장 (중단 시 이어하기 가능)
		savePartialResult(member, sessionId, aptitudeAnswerRequestDto.getStep());

		// 다음 단계 처리
		if (aptitudeAnswerRequestDto.getStep() >= CacheKeyPrefix.APTITUDE_TOTAL_QUESTIONS) {
			// Redis에서 상태 삭제
			aptitudeCacheService.deleteTestProgress(memberId);
			// 최종 결과 계산
			return calculateAndSaveResult(member);
		} else {
			// Redis 상태 업데이트
			aptitudeCacheService.updateTestProgress(memberId, aptitudeAnswerRequestDto.getStep() + 1);

			// 다음 질문 - AI가 대화 맥락을 고려하여 생성
			AptitudeQuestionResponseDto nextQuestion = aptitudeAiService.getNextQuestion(
				member.getId(),
				aptitudeAnswerRequestDto.getStep() + 1
			);

			// 질문을 Redis에 저장
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

	// AI 분석 결과에서 적성 점수 추출 및 Redis 업데이트 (JSON 객체 기반)
	private void updateScoreFromAnalysis(Long memberId, AiAptitudeAnalysisResponseDto aiAnalysis) {
		// NULL 체크 추가
		if (aiAnalysis.getAptitudeType() == null) {
			log.warn("AI 분석 결과 적성 타입이 NULL - 무효 답변으로 처리");
			// 무효 답변인 경우 점수 업데이트 하지 않음
			return;
		}

		try {
			// AI 분석 결과에서 적성 타입 추출
			AptitudeType aptitudeType = AptitudeType.valueOf(aiAnalysis.getAptitudeType().toUpperCase());

			// 신뢰도에 따른 가중치 적용 (높은 신뢰도일수록 더 높은 점수)
			int score = aiAnalysis.getConfidenceScore() > 0.7 ? 2 : 1;

			// 가중치 적용 로그
			log.info("[가중치 테스트] 신뢰도 {}에 따른 점수: {} (기준: 0.7 초과 시 2점, 이하 시 1점)",
				aiAnalysis.getConfidenceScore(), score);

			aptitudeCacheService.updateAptitudeScore(memberId, aptitudeType, score);
			log.debug("적성 점수 업데이트 - memberId: {}, type: {}, score: {}, confidence: {}",
				memberId, aptitudeType, score, aiAnalysis.getConfidenceScore());
		} catch (IllegalArgumentException e) {
			log.warn("AI 응답에서 유효하지 않은 적성 타입: {}", aiAnalysis.getAptitudeType());

			// Fallback: reason에서 키워드 매칭
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

	// String을 AptitudeType Enum으로 안전하게 변환
	private AptitudeType parseAptitudeType(String aptitudeTypeStr) {
		try {
			return AptitudeType.valueOf(aptitudeTypeStr.toUpperCase());
		} catch (IllegalArgumentException | NullPointerException e) {
			log.warn("유효하지 않은 적성 타입 문자열: {}, 기본값 NATURE 반환", aptitudeTypeStr);
			return AptitudeType.NATURE; // 기본값
		}
	}

	// 부분 저장 메소드
	private void savePartialResult(Member member, String sessionId, int currentStep) {

		// 모든 단계에서 저장 (테스트 완료 전까지)
		if (currentStep >= CacheKeyPrefix.APTITUDE_TOTAL_QUESTIONS) {
			return;
		}

		AptitudeTestProgress progress = aptitudeTestProgressRepository.findByMember(member)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_TEST_PROGRESS));

		progress.updatePartialProgress(currentStep, sessionId);

		log.info("[부분 저장] memberId: {}, step: {}, sessionId: {}", member.getId(), currentStep, sessionId);
	}

	private AptitudeTextProgressResponseDto calculateAndSaveResult(Member member) {
		// Redis에서 적성 점수 가져오기
		Map<AptitudeType, Integer> scores = aptitudeCacheService.getAllAptitudeScores(member.getId());

		// 최종 적성 결정
		AptitudeType finalAptitude = aptitudeAiService.calculateFinalAptitude(scores);

		// UserAptitude 결과 업데이트
		UserAptitude userAptitude = userAptitudeRepository.findByMember(member)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_APTITUDE_NOT_FOUND));

		userAptitude.updateAptitudeFromTest(finalAptitude);

		// AptitudeTestProgress 초기화
		aptitudeTestProgressRepository.findByMember(member)
			.ifPresent(AptitudeTestProgress::clearPartialProgress);

		// OnboardingService를 통해 온보딩 상태 완료 처리 위임
		onboardingService.completeOnboardingAfterAptitudeTest(member.getId());

		// Redis에서 세션 ID 가져오기
		String sessionId = aptitudeCacheService.getSessionId(member.getId());

		// 세션의 모든 이력을 완료 처리
		if (sessionId != null) {
			testHistoryService.markSessionAsCompleted(sessionId);
			log.info("[테스트 완료] 최종 적성: {}", finalAptitude);
		}

		// Redis 데이터 정리
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
