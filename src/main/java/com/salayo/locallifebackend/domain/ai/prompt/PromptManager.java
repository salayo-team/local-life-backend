package com.salayo.locallifebackend.domain.ai.prompt;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class PromptManager {

	private final ResourceLoader resourceLoader;
	private final Map<String, String> promptCache = new ConcurrentHashMap<>();

	@Value("${app.ai.prompt.base-path}")
	private String basePath;

	public PromptManager(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public String getPrompt(PromptType type) {
		return promptCache.computeIfAbsent(type.name(), key -> loadPrompt(type));
	}

	private String loadPrompt(PromptType type) {
		try {
			Resource resource = resourceLoader.getResource(basePath + type.getFileName());
			return new String(Files.readAllBytes(resource.getFile().toPath()));
		} catch (IOException e) {
			throw new RuntimeException("프롬프트 파일을 로드할 수 없습니다: " + type.getFileName(), e);
		}
	}
}
