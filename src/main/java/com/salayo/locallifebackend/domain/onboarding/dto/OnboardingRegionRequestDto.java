package com.salayo.locallifebackend.domain.onboarding.dto;

import com.salayo.locallifebackend.domain.onboarding.enums.RegionType;
import jakarta.validation.constraints.NotNull;

/**
 * 선호 지역 특징 선택 요청 DTO
 * TODO: Issue #156 - 지역 특징별 자동 매핑 구현 예정
 */
public record OnboardingRegionRequestDto(
    
    @NotNull(message = "지역 특징을 선택해주세요")
    RegionType regionType  // "URBAN"(도시형), "BALANCED"(균형형), "NATURE"(자연형)
) {}
