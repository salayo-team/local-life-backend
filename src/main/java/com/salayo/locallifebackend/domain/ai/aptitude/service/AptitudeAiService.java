package com.salayo.locallifebackend.domain.ai.aptitude.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salayo.locallifebackend.domain.ai.aptitude.dto.AiAnswerValidationDto;
import com.salayo.locallifebackend.domain.ai.aptitude.dto.AiAptitudeAnalysisResponseDto;
import com.salayo.locallifebackend.domain.ai.aptitude.dto.AiQuestionGenerationDto;
import com.salayo.locallifebackend.domain.ai.aptitude.dto.AptitudeQuestionResponseDto;
import com.salayo.locallifebackend.domain.ai.aptitude.enums.AptitudeType;
import com.salayo.locallifebackend.domain.ai.prompt.PromptManager;
import com.salayo.locallifebackend.domain.ai.prompt.PromptType;
import com.salayo.locallifebackend.domain.ai.service.BaseAiService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AptitudeAiService extends BaseAiService {

	private final AptitudeCacheService cacheService;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public AptitudeAiService(ChatClient chatClient, PromptManager promptManager, 
			AptitudeCacheService cacheService) {
		super(chatClient, promptManager);
		this.cacheService = cacheService;
	}

	/**
	 * AI가 대화 맥락을 고려하여 다음 질문을 동적으로 생성
	 * @param memberId 회원 ID
	 * @param step 현재 단계
	 * @return AI가 생성한 질문과 5가지 예시 답변
	 */
	public AptitudeQuestionResponseDto getNextQuestion(Long memberId, int step) {
		// 대화 히스토리 구성
		String conversationHistory = buildConversationHistory(memberId, step);
		
		// AI에게 다음 질문 생성 요청
		String systemPrompt = """
			당신은 사용자의 적성을 파악하는 전문 상담사입니다.
			사용자와 친근한 반말로 대화하며, 5개의 질문을 통해 다음 5가지 적성 중 가장 적합한 것을 판단해주세요:
			
			1. NATURE (자연친화): 자연 속에서의 삶, 생태적 가치 추구
			2. HISTORY_CULTURE (역사문화): 지역의 역사와 문화에 관심
			3. ART_CREATION (예술창작): 손으로 무언가를 만들고 창작
			4. COMMUNITY (커뮤니티): 사람과 사람을 연결하는 데 즐거움
			5. TECH (디지털기술): 기술로 로컬 문제 해결
			
			대화 규칙:
			- 친근하고 가벼운 톤으로 대화
			- 문장은 50자 이내로 짧게
			- 이전 답변을 고려하여 자연스럽게 연결되는 질문
			- 적성과 무관한 답변을 받으면 자연스럽게 다시 유도
			
			현재 %d/5 단계입니다.
			""".formatted(step);
		
		String userPrompt;
		if (step == 1) {
			// 첫 질문
			userPrompt = """
				적성 검사를 시작합니다.
				사용자를 편안하게 만들면서 적성을 파악할 수 있는 첫 질문을 해주세요.
				각 적성 타입별로 사용자가 답변할 수 있는 예시를 생성해주세요.
				
				JSON 형식으로 응답:
				{
					"question": "질문 내용",
					"example_answers": [
						{"aptitude_type": "NATURE", "example": "자연과 관련된 예시 답변"},
						{"aptitude_type": "HISTORY_CULTURE", "example": "역사문화와 관련된 예시 답변"},
						{"aptitude_type": "ART_CREATION", "example": "예술창작과 관련된 예시 답변"},
						{"aptitude_type": "COMMUNITY", "example": "커뮤니티와 관련된 예시 답변"},
						{"aptitude_type": "TECH", "example": "기술과 관련된 예시 답변"}
					]
				}
				""";
		} else {
			// 후속 질문 - 이전 대화 맥락 포함
			userPrompt = """
				이전 대화:
				%s
				
				위 대화를 바탕으로 사용자의 적성을 더 구체적으로 파악할 수 있는 다음 질문을 해주세요.
				사용자의 이전 답변과 자연스럽게 연결되도록 하세요.
				각 적성 타입별로 사용자가 답변할 수 있는 예시를 생성해주세요.
				
				JSON 형식으로 응답:
				{
					"question": "질문 내용",
					"example_answers": [
						{"aptitude_type": "NATURE", "example": "자연과 관련된 예시 답변"},
						{"aptitude_type": "HISTORY_CULTURE", "example": "역사문화와 관련된 예시 답변"},
						{"aptitude_type": "ART_CREATION", "example": "예술창작과 관련된 예시 답변"},
						{"aptitude_type": "COMMUNITY", "example": "커뮤니티와 관련된 예시 답변"},
						{"aptitude_type": "TECH", "example": "기술과 관련된 예시 답변"}
					]
				}
				""".formatted(conversationHistory);
		}
		
		try {
			String aiResponse = callAi(systemPrompt, userPrompt);
			
			// JSON 파싱
			AiQuestionGenerationDto response = objectMapper.readValue(
				extractJsonFromResponse(aiResponse), 
				AiQuestionGenerationDto.class
			);
			
			log.info("[AI 질문 생성] Step {}: {}", step, response.getQuestion());
			
			// 예시 답변을 List<String>으로 변환
			List<String> examples = new ArrayList<>();
			if (response.getExampleAnswers() != null) {
				for (AiQuestionGenerationDto.ExampleAnswer example : response.getExampleAnswers()) {
					examples.add(example.getExample());
					log.debug("[예시 답변] {}: {}", example.getAptitudeType(), example.getExample());
				}
			}
			
			// 질문을 Redis에 저장
			cacheService.saveQuestion(memberId, step, response.getQuestion());
			
			return new AptitudeQuestionResponseDto(step, response.getQuestion(), examples);
			
		} catch (Exception e) {
			log.error("AI 질문 생성 실패, 폴백 사용 - Step {}: {}", step, e.getMessage());
			return getFallbackQuestion(step);
		}
	}

	// 대화 히스토리 구성
	private String buildConversationHistory(Long memberId, int currentStep) {
		StringBuilder history = new StringBuilder();
		
		for (int i = 1; i < currentStep; i++) {
			// Redis에서 이전 답변과 질문 조회
			String answer = cacheService.getAnswer(memberId, i);
			String question = cacheService.getQuestion(memberId, i);
			
			if (question != null && answer != null) {
				history.append("Q").append(i).append(": ").append(question).append("\n");
				history.append("A").append(i).append(": ").append(answer).append("\n\n");
			}
		}
		
		return history.toString();
	}
	
	/**
	 * 답변 분석 및 적성 판단 - 대화 맥락 고려
	 * 기존 메서드 시그니처 유지하면서 내부 로직 개선
	 */
	public AiAptitudeAnalysisResponseDto analyzeResponse(String userResponse, 
			Long memberId, int step, String currentQuestion) {
		
		// 대화 히스토리 포함
		String conversationHistory = buildConversationHistory(memberId, step);
		
		// 프롬프트 매니저에서 기본 프롬프트 가져오기
		String systemPrompt = promptManager.getPrompt(PromptType.APTITUDE_TEST);
		
		String userPrompt = String.format("""
			대화 맥락:
			%s
			
			현재 질문: %s
			사용자 답변: %s
			
			이 답변을 분석해서 5가지 적성(NATURE:자연친화, HISTORY_CULTURE:역사문화, ART_CREATION:예술창작, COMMUNITY:커뮤니티, TECH:디지털기술) 중 
			어느 것에 가장 가까운지 판단해주세요.
			
			답변이 적성과 무관하거나 너무 짧다면:
			- is_valid를 false로 설정
			- follow_up_question에 자연스럽게 다시 유도하는 질문 작성
			
			반드시 다음 JSON 형식으로만 응답해주세요:
			{
				"is_valid": true,
				"aptitude_type": "적성타입(NATURE/HISTORY_CULTURE/ART_CREATION/COMMUNITY/TECH 중 하나)",
				"confidence_score": 0.8,
				"reason": "판단 이유",
				"key_factors": ["핵심요소1", "핵심요소2"],
				"follow_up_question": null
			}
			""", 
			conversationHistory, currentQuestion, userResponse
		);

		try {
			String aiResponse = callAi(systemPrompt, userPrompt);
			
			// JSON 파싱
			AiAnswerValidationDto validation = objectMapper.readValue(
				extractJsonFromResponse(aiResponse), 
				AiAnswerValidationDto.class
			);
			
			// JSON 파싱 결과 로그 출력 (테스트용)
			log.info("[JSON 파싱 테스트] AI 응답 파싱 성공");
			log.info("[JSON 파싱 테스트] 유효성: {}", validation.isValid());
			log.info("[JSON 파싱 테스트] 적성 타입: {}", validation.getAptitudeType());
			log.info("[JSON 파싱 테스트] 신뢰도 점수: {}", validation.getConfidenceScore());
			log.info("[JSON 파싱 테스트] 판단 이유: {}", validation.getReason());
			log.info("[JSON 파싱 테스트] 핵심 요소: {}", 
				validation.getKeyFactors() != null ? String.join(", ", validation.getKeyFactors()) : "없음");
			
			// 유효하지 않은 답변 처리
			if (!validation.isValid()) {
				log.info("무효 답변 감지 - 재질문: {}", validation.getFollowUpQuestion());
				// 재질문을 다음 질문으로 설정
				if (validation.getFollowUpQuestion() != null) {
					cacheService.saveFollowUpQuestion(memberId, step, validation.getFollowUpQuestion());
				}
			}
			
			return AiAptitudeAnalysisResponseDto.builder()
				.aptitudeType(validation.getAptitudeType())
				.confidenceScore(validation.getConfidenceScore())
				.reason(validation.getReason())
				.keyFactors(validation.getKeyFactors())
				.build();
				
		} catch (Exception e) {
			log.warn("AI 분석 실패, fallback 사용 - step: {}, error: {}", step, e.getMessage());
			return createFallbackAnalysis(userResponse);
		}
	}

	// 기존 시그니처용 오버로드 메서드 (하위 호환성)
	public AiAptitudeAnalysisResponseDto analyzeResponse(String userResponse, 
			AptitudeQuestionResponseDto aptitudeQuestionResponseDto) {
		// 기본값으로 처리 (memberId 없이)
		return analyzeResponse(userResponse, 0L, 
			aptitudeQuestionResponseDto.getOrder(), 
			aptitudeQuestionResponseDto.getQuestion());
	}

	// JSON 추출 헬퍼 메서드
	private String extractJsonFromResponse(String response) {
		// AI 응답에서 JSON 부분만 추출
		int startIdx = response.indexOf("{");
		int endIdx = response.lastIndexOf("}") + 1;
		
		if (startIdx >= 0 && endIdx > startIdx) {
			return response.substring(startIdx, endIdx);
		}
		return response;
	}

	// Fallback 질문 (AI 실패 시)
	private AptitudeQuestionResponseDto getFallbackQuestion(int step) {
		String[][] fallbackData = {
			{"평소에 어떤 활동을 할 때 가장 즐거워?",
				"자연 속에서 산책하거나 텃밭 가꾸기",
				"그림 그리거나 뭔가 만들기",
				"역사 다큐멘터리 보거나 박물관 가기",
				"친구들과 모임하거나 파티하기",
				"새로운 앱 써보거나 코딩하기"},
			{"주말에 시간이 있다면 뭘 하고 싶어?",
				"산이나 바다로 나가서 힐링",
				"DIY 프로젝트나 공예 활동",
				"문화유적지나 전통마을 탐방",
				"동호회 활동이나 봉사활동",
				"해커톤 참가나 앱 개발"},
			{"지역에서 프로젝트를 한다면 어떤 걸 해보고 싶어?",
				"생태 체험 프로그램 운영",
				"지역 특산품 활용한 공예품 제작",
				"지역 역사 스토리텔링 콘텐츠",
				"주민 커뮤니티 공간 운영",
				"지역 문제 해결 플랫폼 개발"},
			{"어떤 환경에서 일할 때 가장 편안해?",
				"자연과 가까운 조용한 환경",
				"창작 도구가 있는 작업실",
				"역사가 깃든 고즈넉한 공간",
				"사람들과 활발히 교류하는 곳",
				"최신 기술 장비가 있는 곳"},
			{"너의 강점이 뭐라고 생각해?",
				"자연과 교감하는 능력",
				"창의적인 아이디어와 손재주",
				"스토리텔링과 문화적 감수성",
				"사람들과 소통하는 능력",
				"기술로 문제 해결하는 능력"}
		};
		
		int index = Math.min(step - 1, fallbackData.length - 1);
		String question = fallbackData[index][0];
		List<String> examples = List.of(
			fallbackData[index][1],
			fallbackData[index][2],
			fallbackData[index][3],
			fallbackData[index][4],
			fallbackData[index][5]
		);
		
		return new AptitudeQuestionResponseDto(step, question, examples);
	}

	// Fallback 분석
	private AiAptitudeAnalysisResponseDto createFallbackAnalysis(String userResponse) {
		String aptitudeType;
		String reason;
		double confidence = 0.5;
		
		if (userResponse.contains("자연") || userResponse.contains("산") || userResponse.contains("바다") 
				|| userResponse.contains("숲") || userResponse.contains("환경")) {
			aptitudeType = "NATURE";
			reason = "자연과 관련된 키워드가 포함되어 있습니다.";
			confidence = 0.6;
		} else if (userResponse.contains("만들") || userResponse.contains("창작") || userResponse.contains("예술")
				|| userResponse.contains("그림") || userResponse.contains("디자인")) {
			aptitudeType = "ART_CREATION";
			reason = "창작과 관련된 키워드가 포함되어 있습니다.";
			confidence = 0.6;
		} else if (userResponse.contains("사람") || userResponse.contains("모임") || userResponse.contains("소통")
				|| userResponse.contains("친구") || userResponse.contains("커뮤니티")) {
			aptitudeType = "COMMUNITY";
			reason = "커뮤니티와 관련된 키워드가 포함되어 있습니다.";
			confidence = 0.6;
		} else if (userResponse.contains("기술") || userResponse.contains("앱") || userResponse.contains("디지털")
				|| userResponse.contains("코딩") || userResponse.contains("개발")) {
			aptitudeType = "TECH";
			reason = "기술과 관련된 키워드가 포함되어 있습니다.";
			confidence = 0.6;
		} else if (userResponse.contains("역사") || userResponse.contains("문화") || userResponse.contains("전통")
				|| userResponse.contains("박물관") || userResponse.contains("유적")) {
			aptitudeType = "HISTORY_CULTURE";
			reason = "역사문화와 관련된 키워드가 포함되어 있습니다.";
			confidence = 0.6;
		} else {
			aptitudeType = "NATURE";
			reason = "기본값으로 자연친화 적성을 추천합니다.";
			confidence = 0.3;
		}
		
		return AiAptitudeAnalysisResponseDto.builder()
			.aptitudeType(aptitudeType)
			.confidenceScore(confidence)
			.reason(reason)
			.keyFactors(List.of("키워드 매칭 (Fallback)"))
			.build();
	}

	// 최종 적성 계산
	public AptitudeType calculateFinalAptitude(Map<AptitudeType, Integer> scores) {
		// 가장 높은 점수를 받은 적성 찾기
		AptitudeType finalAptitude = AptitudeType.NATURE;
		int maxScore = 0;

		log.info("[최종 적성 계산] 점수 현황: {}", scores);
		
		for (Map.Entry<AptitudeType, Integer> entry : scores.entrySet()) {
			if (entry.getValue() > maxScore) {
				maxScore = entry.getValue();
				finalAptitude = entry.getKey();
			}
		}

		// 동점인 경우 우선순위: NATURE > HISTORY_CULTURE > ART_CREATION > COMMUNITY > TECH
		if (maxScore == 0) {
			log.warn("모든 적성 점수가 0입니다. 기본값(NATURE)을 반환합니다.");
		} else {
			log.info("[최종 적성 결정] {} (점수: {})", finalAptitude, maxScore);
		}

		return finalAptitude;
	}
}
