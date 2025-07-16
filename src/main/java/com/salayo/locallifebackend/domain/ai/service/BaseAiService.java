package com.salayo.locallifebackend.domain.ai.service;

import com.salayo.locallifebackend.domain.ai.prompt.PromptManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

@Slf4j
public abstract class BaseAiService {

	protected final ChatClient chatClient;
	protected final PromptManager promptManager;

	protected BaseAiService(ChatClient chatClient, PromptManager promptManager) {
		this.chatClient = chatClient;
		this.promptManager = promptManager;
	}

	@Retryable(
		retryFor = {Exception.class},
		maxAttempts = 3,
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
				response.substring(0, Math.min(response.length(), 100)));

			return response;
		} catch (Exception e) {
			log.error("AI 호출 중 오류 발생", e);
			throw e;
		}
	}
}