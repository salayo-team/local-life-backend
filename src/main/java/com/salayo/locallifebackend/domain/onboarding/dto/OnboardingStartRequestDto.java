package com.salayo.locallifebackend.domain.onboarding.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 온보딩 시작 요청 DTO
 */
@Getter
@NoArgsConstructor
public class OnboardingStartRequestDto {

    private String memberName;
    private String phoneNumber;
}
