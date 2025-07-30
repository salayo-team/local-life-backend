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
import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.member.repository.MemberRepository;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AptitudeService {

	private final UserAptitudeRepository userAptitudeRepository;
	private final AptitudeTestHistoryRepository aptitudeTestHistoryRepository;
	private final MemberRepository memberRepository;
	private final AptitudeAiService aptitudeAiService;
	private final RedisTemplate<String, String> aiAptitudeRedisTemplate;

	private static final int MAX_TEST_COUNT = 5;
	private static final int TOTAL_QUESTIONS = 5;
	private static final String REDIS_KEY_PREFIX = "aptitude:test:";
	private static final long REDIS_TTL_HOURS = 24;

	public AptitudeService(UserAptitudeRepository userAptitudeRepository, AptitudeTestHistoryRepository aptitudeTestHistoryRepository,
		MemberRepository memberRepository, AptitudeAiService aptitudeAiService,
		@Qualifier("aiAptitudeRedisTemplate") RedisTemplate<String, String> aiAptitudeRedisTemplate) {
		this.userAptitudeRepository = userAptitudeRepository;
		this.aptitudeTestHistoryRepository = aptitudeTestHistoryRepository;
		this.memberRepository = memberRepository;
		this.aptitudeAiService = aptitudeAiService;
		this.aiAptitudeRedisTemplate = aiAptitudeRedisTemplate;
	}

	@Transactional
	public AptitudeTestStartResponseDto startTest(Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

		// 테스트 가능 여부 확인
		UserAptitude userAptitude = userAptitudeRepository.findByMember(member).orElse(null);
		if (userAptitude != null && userAptitude.getTestCount() >= MAX_TEST_COUNT) {
			throw new CustomException(ErrorCode.TOO_MANY_REQUESTS);
		}

		/**
		 * 기존 테스트 이력 삭제 -> 사용자가 여러번 테스트 시작했다가 중도 이탈 시 불필요한 이력 삭제가 발생할 수 있음.
		 * - TODO : 테스트 시작이 아닌 완료 시점에만 testCount를 증가시키고 완료되지 않은 테스트는 deleteAllByMember로 삭제하는 정책으로 수정
		 */
		aptitudeTestHistoryRepository.deleteAllByMember(member);

		// Redis에 테스트 진행 상태 저장
		String redisKey = REDIS_KEY_PREFIX + memberId;
		aiAptitudeRedisTemplate.opsForValue().set(redisKey, "1", REDIS_TTL_HOURS, TimeUnit.HOURS);

		// 첫 질문 가져오기
		AptitudeQuestionResponseDto firstQuestion = aptitudeAiService.getNextQuestion(0);

		return new AptitudeTestStartResponseDto(1, TOTAL_QUESTIONS, firstQuestion);
	}

	@Transactional
	public AptitudeTextProgressResponseDto submitAnswer(Long memberId, AptitudeAnswerRequestDto aptitudeAnswerRequestDto) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

		// Redis에서 현재 진행 상태 확인
		String redisKey = REDIS_KEY_PREFIX + memberId;
		String currentStepStr = aiAptitudeRedisTemplate.opsForValue().get(redisKey);
		if (currentStepStr == null) {
			throw new CustomException(ErrorCode.NOT_FOUND_TEST_PROGRESS);
		}

		// 답변 분석
		String aiAnalysis = aptitudeAiService.analyzeResponse(
			aptitudeAnswerRequestDto.getAnswer(),
			aptitudeAiService.getNextQuestion(aptitudeAnswerRequestDto.getStep() - 1)
		);

		// 이력 저장
		AptitudeTestHistory aptitudeTestHistory = AptitudeTestHistory.builder()
			.member(member)
			.step(aptitudeAnswerRequestDto.getStep())
			.questionText(aptitudeAnswerRequestDto.getQuestionText())
			.userResponse(aptitudeAnswerRequestDto.getAnswer())
			.aiResponse(aiAnalysis)
			.build();
		aptitudeTestHistoryRepository.save(aptitudeTestHistory);

		// 다음 단계 처리
		if (aptitudeAnswerRequestDto.getStep() >= TOTAL_QUESTIONS) {
			// Redis에서 상태 삭제
			aiAptitudeRedisTemplate.delete(redisKey);
			// 최종 결과 계산
			return calculateAndSaveResult(member);
		} else {
			// Redis 상태 업데이트
			aiAptitudeRedisTemplate.opsForValue().set(redisKey, String.valueOf(aptitudeAnswerRequestDto.getStep() + 1),
				REDIS_TTL_HOURS, TimeUnit.HOURS);

			// 다음 질문
			AptitudeQuestionResponseDto nextQuestion = aptitudeAiService.getNextQuestion(aptitudeAnswerRequestDto.getStep());
			return new AptitudeTextProgressResponseDto(
				aptitudeAnswerRequestDto.getStep() + 1,
				TOTAL_QUESTIONS,
				nextQuestion,
				false,
				null
			);
		}
	}

	private AptitudeTextProgressResponseDto calculateAndSaveResult(Member member) {
		// 테스트 이력에서 적성 점수 계산
		List<AptitudeTestHistory> histories = aptitudeTestHistoryRepository.findByMemberOrderByStepAsc(member);
		Map<AptitudeType, Integer> scores = analyzeHistories(histories);

		// 최종 적성 결정
		AptitudeType finalAptitude = aptitudeAiService.calculateFinalAptitude(scores);

		// 저장 또는 업데이트
		UserAptitude userAptitude = userAptitudeRepository.findByMember(member)
			.orElse(UserAptitude.builder()
				.member(member)
				.testCount(0)
				.build());

		userAptitude.updateAptitude(finalAptitude);
		userAptitudeRepository.save(userAptitude);

		return new AptitudeTextProgressResponseDto(
			TOTAL_QUESTIONS,
			TOTAL_QUESTIONS,
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
				.build());

		userAptitude.updateAptitude(aptitudeType);
		userAptitudeRepository.save(userAptitude);

		return new AptitudeTestResultResponseDto(aptitudeType);
	}

	@Transactional(readOnly = true)
	public CanRetakeTestResponseDto canRetakeTest(Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

		return userAptitudeRepository.findByMember(member)
			.map(aptitude -> new CanRetakeTestResponseDto(
				aptitude.getTestCount() < MAX_TEST_COUNT,
				aptitude.getTestCount(),
				MAX_TEST_COUNT
			))
			.orElse(new CanRetakeTestResponseDto(true, 0, MAX_TEST_COUNT));
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
}
