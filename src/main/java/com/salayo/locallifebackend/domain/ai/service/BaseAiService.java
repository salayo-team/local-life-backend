package com.salayo.locallifebackend.domain.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salayo.locallifebackend.domain.ai.prompt.PromptManager;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public abstract class BaseAiService {

	protected final ChatClient chatClient;
	protected final PromptManager promptManager;
	private final ObjectMapper objectMapper;

	protected BaseAiService(ChatClient chatClient, PromptManager promptManager) {
		this.chatClient = chatClient;
		this.promptManager = promptManager;
		this.objectMapper = new ObjectMapper();
	}

	@Retryable(
		retryFor = {Exception.class},
		backoff = @Backoff(delay = 1000, multiplier = 2)
	)
	protected String callAi(String systemPrompt, String userInput) {
		try {
			log.debug("AI 요청 시작 - System: {}, User: {}",
				systemPrompt.substring(0, Math.min(systemPrompt.length(), 50)),
				userInput.substring(0, Math.min(userInput.length(), 50)));

			String response = chatClient.prompt()
				.system(systemPrompt)
				.user(userInput)
				.call()
				.content();

			log.debug("AI 응답 완료 - Response: {}",
				response != null ? response.substring(0, Math.min(response.length(), 100)) : "null");

			return response;
		} catch (Exception e) {
			log.error("AI 호출 중 오류 발생", e);
			throw e;
		}
	}

	/**
	 * AI 응답을 JSON으로 파싱하여 지정된 타입으로 변환
	 * @param response AI 응답 문자열
	 * @param responseType 변환할 타입 클래스
	 * @return 파싱된 응답 객체
	 */
	protected <T> T parseJsonResponse(String response, Class<T> responseType) {
		if (response == null || response.trim().isEmpty()) {
			throw new CustomException(ErrorCode.AI_RESPONSE_INVALID_FORMAT, "AI 응답이 비어있습니다.");
		}

		try {
			// JSON 블록 추출 (```json ... ``` 형태 처리)
			String jsonContent = extractJsonContent(response);
			return objectMapper.readValue(jsonContent, responseType);
		} catch (JsonProcessingException e) {
			log.error("AI 응답 JSON 파싱 실패: {}", response, e);
			throw new CustomException(ErrorCode.AI_RESPONSE_INVALID_FORMAT);
		}
	}

	/**
	 * AI 응답에서 JSON 내용만 추출
	 * @param response 원본 응답
	 * @return JSON 문자열
	 */
	private String extractJsonContent(String response) {
		String trimmed = response.trim();

		// ```json 블록 처리
		if (trimmed.contains("```json")) {
			int startIdx = trimmed.indexOf("```json") + 7;
			int endIdx = trimmed.lastIndexOf("```");
			if (endIdx > startIdx) {
				return trimmed.substring(startIdx, endIdx).trim();
			}
		}

		// {} 또는 [] 로 시작하는 순수 JSON
		if ((trimmed.startsWith("{") && trimmed.endsWith("}")) ||
			(trimmed.startsWith("[") && trimmed.endsWith("]"))) {
			return trimmed;
		}

		// JSON 블록 찾기
		int jsonStart = Math.max(trimmed.indexOf("{"), trimmed.indexOf("["));
		int jsonEnd = Math.max(trimmed.lastIndexOf("}"), trimmed.lastIndexOf("]"));

		if (jsonStart >= 0 && jsonEnd > jsonStart) {
			return trimmed.substring(jsonStart, jsonEnd + 1);
		}

		return trimmed;
	}
}