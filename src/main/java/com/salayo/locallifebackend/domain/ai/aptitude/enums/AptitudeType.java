package com.salayo.locallifebackend.domain.ai.aptitude.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AptitudeType {
	NATURE("자연친화", "자연 속에서의 삶, 생태적 가치와 조화롭게 살아가는 것을 추구"),
	HISTORY_CULTURE("역사문화", "지역의 역사와 문화에 관심이 많고, 콘텐츠로 풀어내는 능력 보유"),
	ART_CREATION("예술창작", "손으로 무언가를 만들고 창작하며, 자신만의 브랜드를 만들고자 함"),
	COMMUNITY("커뮤니티", "사람과 사람, 지역과 사람을 연결하는 데 즐거움을 느낌"),
	TECH("디지털기술", "기술로 로컬 문제를 해결하고자 하며, ICT/데이터/플랫폼에 관심");

	private final String title;
	private final String description;
}

