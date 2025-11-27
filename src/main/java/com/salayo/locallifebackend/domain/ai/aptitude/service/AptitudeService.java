package com.salayo.locallifebackend.domain.ai.aptitude.service;

import com.salayo.locallifebackend.domain.ai.aptitude.dto.AptitudeAnswerRequestDto;
import com.salayo.locallifebackend.domain.ai.aptitude.dto.AptitudeQuestionResponseDto;
import com.salayo.locallifebackend.domain.ai.aptitude.dto.AptitudeResumeResponseDto;
import com.salayo.locallifebackend.domain.ai.aptitude.dto.AptitudeTestStartResponseDto;
import com.salayo.locallifebackend.domain.ai.aptitude.dto.AptitudeTextProgressResponseDto;
import com.salayo.locallifebackend.domain.ai.aptitude.dto.AptitudeTestResultResponseDto;
import com.salayo.locallifebackend.domain.ai.aptitude.dto.CanRetakeTestResponseDto;
import com.salayo.locallifebackend.domain.ai.aptitude.entity.AptitudeTestHistory;
import com.salayo.locallifebackend.domain.ai.aptitude.entity.UserAptitude;
import com.salayo.locallifebackend.domain.ai.aptitude.enums.AptitudeType;
import com.salayo.locallifebackend.domain.ai.aptitude.repository.UserAptitudeRepository;
import com.salayo.locallifebackend.domain.ai.aptitude.dto.AiAptitudeAnalysisResponseDto;
import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.member.repository.MemberRepository;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import com.salayo.locallifebackend.global.util.CacheKeyPrefix;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AptitudeService {

	private final UserAptitudeRepository userAptitudeRepository;
	private final AptitudeTestHistoryService testHistoryService;
	private final MemberRepository memberRepository;
	private final AptitudeAiService aptitudeAiService;
	private final AptitudeCacheService aptitudeCacheService;

	public AptitudeService(UserAptitudeRepository userAptitudeRepository,
			AptitudeTestHistoryService testHistoryService,
			MemberRepository memberRepository,
			AptitudeAiService aptitudeAiService,
			AptitudeCacheService aptitudeCacheService) {
			this.userAptitudeRepository = userAptitudeRepository;
			this.testHistoryService = testHistoryService;
			this.memberRepository = memberRepository;
			this.aptitudeAiService = aptitudeAiService;
			this.aptitudeCacheService = aptitudeCacheService;
		}

	@Transactional
	public AptitudeTestStartResponseDto startTest(Long memberId) {
		Member member = memberRepository.findActiveByIdOrThrow(memberId);

		// 테스트 가능 여부 확인
		UserAptitude userAptitude = userAptitudeRepository.findByMember(member)
			.orElseGet(() -> {
				log.info("새로운 사용자의 UserAptitudes를 생성합니다. memberId: {}", memberId);

				return UserAptitude.createNew(member);
			});

		if (userAptitude.getIsOnboardingCompleted()) {
			log.info("온보딩 완료 사용자입니다. 마이페이지에서 테스트 횟수를 검사합니다. count: {}", userAptitude.getMypageTestCount());

			if (userAptitude.getMypageTestCount() >= CacheKeyPrefix.APTITUDE_MAX_TEST_COUNT) {
				throw new CustomException(ErrorCode.APTITUDE_TEST_LIMIT_EXCEEDED);
			}
			userAptitude.incrementMypageTestCount();
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

	@Transactional
	public AptitudeTextProgressResponseDto submitAnswer(Long memberId, AptitudeAnswerRequestDto aptitudeAnswerRequestDto) {
		Member member = memberRepository.findActiveByIdOrThrow(memberId);

		// Redis에서 현재 진행 상태 확인
		String currentStepStr = aptitudeCacheService.getTestProgress(memberId);
		if (currentStepStr == null) {
			throw new CustomException(ErrorCode.NOT_FOUND_TEST_PROGRESS);
		}
		
		// Redis에서 세션 ID 가져오기
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

	private AptitudeTextProgressResponseDto calculateAndSaveResult(Member member) {
		// Redis에서 적성 점수 가져오기
		Map<AptitudeType, Integer> scores = aptitudeCacheService.getAllAptitudeScores(member.getId());

		// 최종 적성 결정
		AptitudeType finalAptitude = aptitudeAiService.calculateFinalAptitude(scores);
		
		// Redis에서 세션 ID 가져오기
		String sessionId = aptitudeCacheService.getSessionId(member.getId());

		// 저장 또는 업데이트
		UserAptitude userAptitude = userAptitudeRepository.findByMember(member)
			.orElse(UserAptitude.builder()
				.member(member)
				.aptitudeType(finalAptitude)
				.testCount(0)
				.mypageTestCount(0)
				.isOnboardingCompleted(false)
				.build());

		// 온보딩 완료 여부에 따라 다르게 처리
		if (!userAptitude.getIsOnboardingCompleted()) {
			userAptitude.updateAptitudeFromOnboarding(finalAptitude);
		} else {
			userAptitude.updateAptitudeFromMypage(finalAptitude);
		}

		// 부분 저장 정보 초기화
		userAptitude.clearPartialProgress();
		userAptitudeRepository.save(userAptitude);
		
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
				.aptitudeType(aptitudeType)
				.testCount(0)
				.mypageTestCount(0)
				.isOnboardingCompleted(false)
				.build());

		userAptitude.updateAptitudeFromOnboarding(aptitudeType);
		userAptitudeRepository.save(userAptitude);

		return new AptitudeTestResultResponseDto(aptitudeType);
	}

	@Transactional(readOnly = true)
	public CanRetakeTestResponseDto canRetakeTest(Long memberId) {
		Member member = memberRepository.findActiveByIdOrThrow(memberId);

		return userAptitudeRepository.findByMember(member)
			.map(aptitude -> {
				// 온보딩 미완료 시 항상 가능
				if (!aptitude.getIsOnboardingCompleted()) {
					return new CanRetakeTestResponseDto(true, 0, CacheKeyPrefix.APTITUDE_MAX_TEST_COUNT);
				}
				// 온보딩 완료 후 마이페이지 테스트 횟수 확인
				return new CanRetakeTestResponseDto(
					aptitude.getMypageTestCount() < CacheKeyPrefix.APTITUDE_MAX_TEST_COUNT,
					aptitude.getMypageTestCount(),
					CacheKeyPrefix.APTITUDE_MAX_TEST_COUNT
				);
			})
			.orElse(new CanRetakeTestResponseDto(true, 0, CacheKeyPrefix.APTITUDE_MAX_TEST_COUNT));
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
		if (currentStep < CacheKeyPrefix.APTITUDE_TOTAL_QUESTIONS) {
			UserAptitude userAptitude = userAptitudeRepository.findByMember(member)
				.orElse(UserAptitude.builder()
					.member(member)
					.aptitudeType(AptitudeType.NATURE)
					.testCount(0)
					.mypageTestCount(0)
					.isOnboardingCompleted(false)
					.build());

			userAptitude.updatePartialProgress(currentStep, sessionId);
			userAptitudeRepository.save(userAptitude);

			log.info("[부분 저장] memberId: {}, step: {}, sessionId: {}",
				member.getId(), currentStep, sessionId);
		}
	}

	// 이어하기 기능
	@Transactional
	public AptitudeResumeResponseDto resumeTest(Long memberId) {
		Member member = memberRepository.findActiveByIdOrThrow(memberId);

		// UserAptitude에서 부분 저장 정보 확인
		UserAptitude userAptitude = userAptitudeRepository.findByMember(member).orElse(null);

		if (userAptitude != null && userAptitude.getPartialSessionId() != null) {
			String sessionId = userAptitude.getPartialSessionId();
			Integer lastStep = userAptitude.getLastPartialStep();

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
