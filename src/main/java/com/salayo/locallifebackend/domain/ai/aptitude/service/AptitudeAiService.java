package com.salayo.locallifebackend.domain.ai.aptitude.service;

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

	private static final List<AptitudeQuestionResponseDto> QUESTION_POOL = createQuestionPool();

	public AptitudeAiService(ChatClient chatClient, PromptManager promptManager) {
		super(chatClient, promptManager);
	}

	private static List<AptitudeQuestionResponseDto> createQuestionPool() {
		List<AptitudeQuestionResponseDto> questions = new ArrayList<>();

		questions.add(new AptitudeQuestionResponseDto(1,
			"너가 제일 좋아하는 활동은 뭐야?",
			List.of(
				"텃밭을 가꾸거나 자연을 산책하는 것",
				"뭔가를 직접 만들고 꾸미는 것",
				"책이나 다큐로 지역 이야기를 탐색하는 것",
				"새로운 사람을 만나고 소통하는 것",
				"앱, 툴, 기술을 써보며 문제를 해결하는 것"
			)));

		questions.add(new AptitudeQuestionResponseDto(2,
			"주말에 뭐하면서 시간 보내는 걸 좋아해?",
			List.of(
				"산이나 바다로 나가서 힐링하기",
				"DIY 프로젝트나 공예 활동하기",
				"박물관이나 역사 유적지 방문하기",
				"친구들과 모임이나 파티 즐기기",
				"새로운 앱이나 기술 체험해보기"
			)));

		questions.add(new AptitudeQuestionResponseDto(3,
			"만약 지역에서 프로젝트를 한다면 어떤 걸 하고 싶어?",
			List.of(
				"생태 체험 프로그램이나 친환경 농업",
				"지역 특산품을 활용한 수공예품 제작",
				"지역 역사나 문화를 소개하는 콘텐츠 제작",
				"지역 주민들을 위한 커뮤니티 공간 운영",
				"지역 문제 해결을 위한 앱이나 플랫폼 개발"
			)));

		questions.add(new AptitudeQuestionResponseDto(4,
			"어떤 환경에서 일할 때 가장 행복해?",
			List.of(
				"자연과 가까운 조용한 환경",
				"창작 도구들이 가득한 작업실",
				"역사와 이야기가 있는 공간",
				"사람들과 활발히 교류하는 공간",
				"최신 기술과 장비가 갖춰진 곳"
			)));

		questions.add(new AptitudeQuestionResponseDto(5,
			"너의 강점은 뭐라고 생각해?",
			List.of(
				"자연과 교감하고 지속가능한 삶을 추구하는 것",
				"손재주가 좋고 창의적인 아이디어가 많은 것",
				"스토리텔링과 문화적 감수성",
				"사람들과 소통하고 연결하는 능력",
				"기술을 활용해 효율적으로 문제를 해결하는 것"
			)));

		return questions;
	}

	public AptitudeQuestionResponseDto getNextQuestion(int step) {
		if (step >= 0 && step < QUESTION_POOL.size()) {
			return QUESTION_POOL.get(step);
		}
		return QUESTION_POOL.get(0);
	}

	public String analyzeResponse(String userResponse, AptitudeQuestionResponseDto aptitudeQuestionResponseDto) {
		String systemPrompt = promptManager.getPrompt(PromptType.APTITUDE_TEST);
		String userPrompt = String.format(
			"질문: %s\n사용자 답변: %s\n\n" +
				"이 답변을 분석해서 5가지 적성(자연친화, 역사문화, 예술창작, 커뮤니티, 디지털기술) 중 " +
				"어느 것에 가장 가까운지 판단하고, 그 이유를 간단히 설명해줘.",
			aptitudeQuestionResponseDto.getQuestion(), userResponse
		);

		return callAi(systemPrompt, userPrompt);
	}

	public AptitudeType calculateFinalAptitude(Map<AptitudeType, Integer> scores) {
		// 가장 높은 점수를 받은 적성 찾기
		AptitudeType finalAptitude = AptitudeType.NATURE;
		int maxScore = 0;

		for (Map.Entry<AptitudeType, Integer> entry : scores.entrySet()) {
			if (entry.getValue() > maxScore) {
				maxScore = entry.getValue();
				finalAptitude = entry.getKey();
			}
		}

		// 동점인 경우 우선순위: NATURE > HISTORY_CULTURE > ART_CREATION > COMMUNITY > TECH
		if (maxScore == 0) {
			log.warn("모든 적성 점수가 0입니다. 기본값(NATURE)을 반환합니다.");
		}

		return finalAptitude;
	}
}
