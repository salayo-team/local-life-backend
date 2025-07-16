package com.salayo.locallifebackend.domain.ai.service;

import com.salayo.locallifebackend.domain.ai.prompt.PromptManager;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiTestService extends BaseAiService {

	protected AiTestService(ChatClient chatClient,
		PromptManager promptManager) {
		super(chatClient, promptManager);
	}
}
