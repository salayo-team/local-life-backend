//package com.salayo.locallifebackend.domain.ai.controller;
//
//import com.salayo.locallifebackend.domain.ai.service.BaseAiService;
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/ai/test")
//public class AiTestController {
//
//	private final ChatClient chatClient;
//	private final BaseAiService baseAiService;
//
//	public AiTestController(ChatClient chatClient, BaseAiService baseAiService) {
//		this.chatClient = chatClient;
//		this.baseAiService = baseAiService;
//	}
//
//	@PostMapping("/chat")
//	public String testChat(@RequestBody String message) {
//		return chatClient.prompt()
//			.user(message)
//			.call()
//			.content();
//	}
//
//	@GetMapping("/health")
//	public String health() {
//		return "AI Service 작동중";
//	}
//}
