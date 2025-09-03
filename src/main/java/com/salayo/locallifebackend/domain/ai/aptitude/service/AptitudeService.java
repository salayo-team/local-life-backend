package com.salayo.locallifebackend.domain.ai.aptitude.service;

import com.salayo.locallifebackend.domain.ai.aptitude.dto.AptitudeAnswerRequestDto;
import com.salayo.locallifebackend.domain.ai.aptitude.dto.AptitudeQuestionResponseDto;
import com.salayo.locallifebackend.domain.ai.aptitude.dto.AptitudeTestStartResponseDto;
import com.salayo.locallifebackend.domain.ai.aptitude.dto.AptitudeTextProgressResponseDto;
import com.salayo.locallifebackend.domain.ai.aptitude.dto.AptitudeTestResultResponseDto;
import com.salayo.locallifebackend.domain.ai.aptitude.dto.CanRetakeTestResponseDto;
import com.salayo.locallifebackend.domain.ai.aptitude.entity.AptitudeTestHistory;
import com.salayo.locallifebackend.domain.ai.aptitude.entity.UserAptitude;
import com.salayo.locallifebackend.domain.ai.aptitude.enums.AptitudeType;
import com.salayo.locallifebackend.domain.ai.aptitude.repository.AptitudeTestHistoryRepository;
import com.salayo.locallifebackend.domain.ai.aptitude.repository.UserAptitudeRepository;
import com.salayo.locallifebackend.domain.ai.aptitude.dto.AiAptitudeAnalysisResponseDto;
import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.member.repository.MemberRepository;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import com.salayo.locallifebackend.global.util.CacheKeyPrefix;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AptitudeService {

	private final UserAptitudeRepository userAptitudeRepository;
	private final AptitudeTestHistoryRepository aptitudeTestHistoryRepository;
	private final MemberRepository memberRepository;
	private final AptitudeAiService aptitudeAiService;
	private final AptitudeCacheService aptitudeCacheService;

	public AptitudeService(UserAptitudeRepository userAptitudeRepository, 
			AptitudeTestHistoryRepository aptitudeTestHistoryRepository,
			MemberRepository memberRepository, 
			AptitudeAiService aptitudeAiService,
			AptitudeCacheService aptitudeCacheService) {
			this.userAptitudeRepository = userAptitudeRepository;
			this.aptitudeTestHistoryRepository = aptitudeTestHistoryRepository;
			this.memberRepository = memberRepository;
			this.aptitudeAiService = aptitudeAiService;
			this.aptitudeCacheService = aptitudeCacheService;
		}

	@Transactional
	public AptitudeTestStartResponseDto startTest(Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

		// 테스트 가능 여부 확인
		UserAptitude userAptitude = userAptitudeRepository.findByMember(member).orElse(null);
		
		// 마이페이지에서 호출된 경우 (이미 온보딩을 완료한 경우)
		if (userAptitude != null && userAptitude.getIsOnboardingCompleted()) {
			// 마이페이지 테스트 횟수 확인 (최대 5회)
			if (userAptitude.getMypageTestCount() >= CacheKeyPrefix.APTITUDE_MAX_TEST_COUNT) {
				throw new CustomException(ErrorCode.APTITUDE_TEST_LIMIT_EXCEEDED);
			}
		}
		// 온보딩에서 처음 호출된 경우는 제한 없음

		/**
		 * 기존 테스트 이력 삭제 -> 사용자가 여러번 테스트 시작했다가 중도 이탈 시 불필요한 이력 삭제가 발생할 수 있음.
		 * - TODO : 테스트 시작이 아닌 완료 시점에만 testCount를 증가시키고 완료되지 않은 테스트는 deleteAllByMember로 삭제하는 정책으로 수정
		 */
		aptitudeTestHistoryRepository.deleteAllByMember(member);
		
		// Redis 기존 데이터 정리 (중복 방지)
		aptitudeCacheService.deleteAllTestData(memberId);

		// Redis에 테스트 진행 상태 저장
		aptitudeCacheService.saveTestProgress(memberId, "1");

		// 첫 질문 가져오기
		AptitudeQuestionResponseDto firstQuestion = aptitudeAiService.getNextQuestion(0);

		return new AptitudeTestStartResponseDto(1, CacheKeyPrefix.APTITUDE_TOTAL_QUESTIONS, firstQuestion);
	}

	@Transactional
	public AptitudeTextProgressResponseDto submitAnswer(Long memberId, AptitudeAnswerRequestDto aptitudeAnswerRequestDto) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

		// Redis에서 현재 진행 상태 확인
		String currentStepStr = aptitudeCacheService.getTestProgress(memberId);
		if (currentStepStr == null) {
			throw new CustomException(ErrorCode.NOT_FOUND_TEST_PROGRESS);
		}

		// 답변 분석 (JSON 파싱된 객체 반환)
		AiAptitudeAnalysisResponseDto aiAnalysis = aptitudeAiService.analyzeResponse(
			aptitudeAnswerRequestDto.getAnswer(),
			aptitudeAiService.getNextQuestion(aptitudeAnswerRequestDto.getStep() - 1)
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
		
		// 디버깅용 로그 (테스트 후 제거)
		Map<AptitudeType, Integer> currentScores = aptitudeCacheService.getAllAptitudeScores(memberId);
		log.info("[TEST] 현재 적성 점수 상태: {}", currentScores);

		// 이력 저장 (AI 분석 결과를 JSON 문자열로 저장)
		String aiResponseStr = String.format("%s - %s (신뢰도: %.2f)", 
			aiAnalysis.getAptitudeType(), 
			aiAnalysis.getReason(), 
			aiAnalysis.getConfidenceScore());
			
		AptitudeTestHistory aptitudeTestHistory = AptitudeTestHistory.builder()
			.member(member)
			.step(aptitudeAnswerRequestDto.getStep())
			.questionText(aptitudeAnswerRequestDto.getQuestionText())
			.userResponse(aptitudeAnswerRequestDto.getAnswer())
			.aiResponse(aiResponseStr)
			.build();
		aptitudeTestHistoryRepository.save(aptitudeTestHistory);

		// 다음 단계 처리
		if (aptitudeAnswerRequestDto.getStep() >= CacheKeyPrefix.APTITUDE_TOTAL_QUESTIONS) {
			// Redis에서 상태 삭제
			aptitudeCacheService.deleteTestProgress(memberId);
			// 최종 결과 계산
			return calculateAndSaveResult(member);
		} else {
			// Redis 상태 업데이트
			aptitudeCacheService.updateTestProgress(memberId, aptitudeAnswerRequestDto.getStep() + 1);

			// 다음 질문
			AptitudeQuestionResponseDto nextQuestion = aptitudeAiService.getNextQuestion(aptitudeAnswerRequestDto.getStep());
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

		// 저장 또는 업데이트
		UserAptitude userAptitude = userAptitudeRepository.findByMember(member)
			.orElse(UserAptitude.builder()
				.member(member)
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
		
		userAptitudeRepository.save(userAptitude);
		
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
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

		UserAptitude userAptitude = userAptitudeRepository.findByMember(member)
			.orElse(UserAptitude.builder()
				.member(member)
				.testCount(0)
				.mypageTestCount(0)
				.isOnboardingCompleted(false)
				.build());

		// 수동 선택은 현재 온보딩에서만 가능하므로 온보딩 완료 처리 됨
		// TODO : 수동 선택은 MyPage에서도 추후에 수동 선택해서 수정이 가능함.
		userAptitude.updateAptitudeFromOnboarding(aptitudeType);
		userAptitudeRepository.save(userAptitude);

		return new AptitudeTestResultResponseDto(aptitudeType);
	}

	@Transactional(readOnly = true)
	public CanRetakeTestResponseDto canRetakeTest(Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

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

	private Map<AptitudeType, Integer> analyzeHistories(List<AptitudeTestHistory> histories) {
		Map<AptitudeType, Integer> scores = new HashMap<>();
		for (AptitudeType type : AptitudeType.values()) {
			scores.put(type, 0);
		}

		/**
		 * 정규식 패턴 생성 - 모든 적성 타입을 OR로 연결
		 * TODO : HashMap 최적화
		 */
		String pattern = Arrays.stream(AptitudeType.values())
			.map(type -> type.name() + "|" + type.getTitle())
			.collect(Collectors.joining("|"));
		Pattern aptitudePattern = Pattern.compile("(" + pattern + ")");

		// AI 응답에서 적성 키워드 추출 및 점수 계산
		for (AptitudeTestHistory history : histories) {
			String aiResponse = history.getAiResponse();
			if (aiResponse != null) {
				Matcher matcher = aptitudePattern.matcher(aiResponse);
				while (matcher.find()) {
					String matchedCode = matcher.group();
					// 매칭된 코드가 어떤 적성 타입인지 확인
					for (AptitudeType type : AptitudeType.values()) {
						if (matchedCode.equals(type.name()) || matchedCode.equals(type.getTitle())) {
							scores.put(type, scores.get(type) + 1);
							break;
						}
					}
				}
			}
		}

		return scores;
	}

	// AI 분석 결과에서 적성 점수 추출 및 Redis 업데이트 (JSON 객체 기반)
	private void updateScoreFromAnalysis(Long memberId, AiAptitudeAnalysisResponseDto aiAnalysis) {
		try {
			// AI 분석 결과에서 적성 타입 추출
			AptitudeType aptitudeType = AptitudeType.valueOf(aiAnalysis.getAptitudeType());
			
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
}
