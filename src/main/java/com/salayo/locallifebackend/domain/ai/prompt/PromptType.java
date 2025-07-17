package com.salayo.locallifebackend.domain.ai.prompt;

public enum PromptType {
	APTITUDE_TEST("aptitude-test.txt"),
	LOCAL_SPECIALIZATION("local-specialization.txt"),
	PROGRAM_SUMMARY("program-summary.txt");

	private final String fileName;

	PromptType(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}
}
