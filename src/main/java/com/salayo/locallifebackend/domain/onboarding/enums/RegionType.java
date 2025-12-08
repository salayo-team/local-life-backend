package com.salayo.locallifebackend.domain.onboarding.enums;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;


/**
 * 선호 지역 특징
 */
@Getter
public enum RegionType {
	URBAN("도시형", Arrays.asList("서울", "부산", "인천", "대구", "광주", "대전", "울산")),
	BALANCED("균형형", Arrays.asList("경기", "충남", "충북", "경남", "경북")),
	NATURE("자연형", Arrays.asList("강원", "제주", "전남", "전북"));

	private final String description;
	private final List<String> regions;

	RegionType(String description, List<String> regions) {
		this.description = description;
		this.regions = regions;
	}
}
