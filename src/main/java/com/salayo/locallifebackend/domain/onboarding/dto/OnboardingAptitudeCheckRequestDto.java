package com.salayo.locallifebackend.domain.onboarding.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 적성 인지 여부 확인 요청 DTO
 */
public record OnboardingAptitudeCheckRequestDto(
    @NotNull(message = "적성 인지 여부를 선택해주세요")
    Boolean knowsAptitude  // true: 알고있다, false: 모른다
) {}
