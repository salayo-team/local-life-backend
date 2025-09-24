package com.salayo.locallifebackend.domain.onboarding.service;

import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.member.repository.MemberRepository;
import com.salayo.locallifebackend.domain.onboarding.dto.OnboardingProgressResponseDto;
import com.salayo.locallifebackend.domain.onboarding.dto.OnboardingRegionRequestDto;
import com.salayo.locallifebackend.domain.onboarding.dto.OnboardingAptitudeCheckRequestDto;
import com.salayo.locallifebackend.domain.onboarding.dto.OnboardingStatusResponseDto;
import com.salayo.locallifebackend.domain.onboarding.entity.OnboardingProgress;
import com.salayo.locallifebackend.domain.onboarding.enums.OnboardingStep;
import com.salayo.locallifebackend.domain.onboarding.enums.RegionType;
import com.salayo.locallifebackend.domain.onboarding.repository.OnboardingProgressRepository;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
public class OnboardingService {
    
    private final OnboardingProgressRepository onboardingProgressRepository;
    private final MemberRepository memberRepository;

	public OnboardingService(OnboardingProgressRepository onboardingProgressRepository, MemberRepository memberRepository) {
		this.onboardingProgressRepository = onboardingProgressRepository;
		this.memberRepository = memberRepository;
	}

	/**
     * 온보딩 시작
     */
    @Transactional
    public OnboardingProgressResponseDto startOnboarding(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        
        // 기존 온보딩 진행 상태 확인
        OnboardingProgress progress = onboardingProgressRepository.findByMember(member)
            .orElseGet(() -> {
                // 새로운 온보딩 시작
                String sessionId = generateSessionId(memberId);
                OnboardingProgress newProgress = OnboardingProgress.builder()
                    .member(member)
                    .currentStep(OnboardingStep.MEMBER_INFO)
                    .isCompleted(false)
                    .sessionId(sessionId)
                    .build();
                return onboardingProgressRepository.save(newProgress);
            });
        
        // 이미 완료된 경우
        if (progress.isCompleted()) {
            log.info("온보딩이 이미 완료됨 - memberId: {}", memberId);
            return OnboardingProgressResponseDto.createCompleteResponse(
                progress.getSessionId(), 
                progress.getRegionType()
            );
        }
        
        log.info("온보딩 시작 - memberId: {}, sessionId: {}", memberId, progress.getSessionId());
        return OnboardingProgressResponseDto.createStartResponse(progress.getSessionId());
    }
    
    /**
     * 선호 지역 특징 선택
     * TODO: Issue #156 - 지역 특징별 자동 매핑 구현 예정
     */
    @Transactional
    public OnboardingProgressResponseDto selectRegion(Long memberId, OnboardingRegionRequestDto regionRequestDto) {
        OnboardingProgress progress = getOnboardingProgress(memberId);
        
        // 현재 단계 검증
        if (progress.getCurrentStep() != OnboardingStep.MEMBER_INFO && 
            progress.getCurrentStep() != OnboardingStep.REGION_SELECT) {
            throw new CustomException(ErrorCode.INVALID_ONBOARDING_STEP);
        }
        
        // 지역 특징 타입 저장
        RegionType regionType = regionRequestDto.regionType();
        progress.updateRegion(regionType);
        
        // 다음 단계로 이동
        progress.moveToNextStep(false);
        onboardingProgressRepository.save(progress);
        
        log.info("선호 지역 특징 선택 완료 - memberId: {}, regionType: {}", memberId, regionType);
        return OnboardingProgressResponseDto.createRegionCompleteResponse(
            progress.getSessionId(), 
            regionType
        );
    }
    
    /**
     * 적성 인지 여부 확인
     */
    @Transactional
    public OnboardingProgressResponseDto checkAptitudeKnowledge(Long memberId, 
                                                               OnboardingAptitudeCheckRequestDto aptitudeCheckRequestDto) {
        OnboardingProgress progress = getOnboardingProgress(memberId);
        
        // 현재 단계 검증
        if (progress.getCurrentStep() != OnboardingStep.APTITUDE_CHECK) {
            throw new CustomException(ErrorCode.INVALID_ONBOARDING_STEP);
        }
        
        // 적성 인지 여부 저장
        boolean knowsAptitude = aptitudeCheckRequestDto.knowsAptitude();
        progress.updateKnowsAptitude(knowsAptitude);
        
        // 다음 단계로 이동 (인지 여부에 따라 분기)
        progress.moveToNextStep(knowsAptitude);
        onboardingProgressRepository.save(progress);
        
        log.info("적성 인지 여부 확인 - memberId: {}, knowsAptitude: {}", memberId, knowsAptitude);
        
        // 응답 생성 (분기 처리)
        if (knowsAptitude) {
            // 적성을 알고 있는 경우 → 수동 선택 페이지로
            return OnboardingProgressResponseDto.createManualAptitudeResponse(
                progress.getSessionId(), 
                progress.getRegionType()
            );
        } else {
            // 적성을 모르는 경우 → AI 검사 페이지로
            return OnboardingProgressResponseDto.createAiTestResponse(
                progress.getSessionId(), 
                progress.getRegionType()
            );
        }
    }
    
    /**
     * 온보딩 완료 처리
     * (적성 선택 또는 AI 검사 완료 후 호출)
     */
    @Transactional
    public OnboardingProgressResponseDto completeOnboarding(Long memberId) {
        OnboardingProgress progress = getOnboardingProgress(memberId);
        
        // 적성 단계가 아닌 경우 에러
        if (progress.getCurrentStep() != OnboardingStep.APTITUDE_MANUAL && 
            progress.getCurrentStep() != OnboardingStep.APTITUDE_AI_TEST) {
            throw new CustomException(ErrorCode.INVALID_ONBOARDING_STEP);
        }
        
        // 온보딩 완료 처리
        progress.complete();
        onboardingProgressRepository.save(progress);
        
        log.info("온보딩 완료 - memberId: {}, regionType: {}", memberId, progress.getRegionType());
        return OnboardingProgressResponseDto.createCompleteResponse(
            progress.getSessionId(), 
            progress.getRegionType()
        );
    }

    /**
     * 온보딩 이어하기(Resume) 여부 판단을 위한 진행 상태 조회
     */
    @Transactional(readOnly = true)
    public OnboardingStatusResponseDto getOnboardingStatus(Long memberId) {
        return onboardingProgressRepository.findByMemberId(memberId)
            .filter(progress -> !progress.isCompleted())  // 완료되지 않은 경우만
            .map(progress -> OnboardingStatusResponseDto.inProgress(progress.getCurrentStep(), progress.getSessionId()))
            .orElseGet(OnboardingStatusResponseDto::complete);  // 없거나 완료된 경우
    }

    /**
     * 현재 온보딩 단계의 상세 정보 조회
     */
    @Transactional(readOnly = true)
    public OnboardingProgressResponseDto getCurrentProgress(Long memberId) {
        OnboardingProgress progress = getOnboardingProgress(memberId);
        
        return OnboardingProgressResponseDto.builder()
            .sessionId(progress.getSessionId())
            .currentStep(progress.getCurrentStep())
            .isCompleted(progress.isCompleted())
            .regionType(progress.getRegionType())
            .knowsAptitude(progress.getKnowsAptitude())
            .build();
    }
    
    /**
     * 온보딩 진행 상태 조회 (내부 사용)
     */
    private OnboardingProgress getOnboardingProgress(Long memberId) {
        return onboardingProgressRepository.findByMemberId(memberId)
            .orElseThrow(() -> {
                log.error("온보딩 진행 정보 없음 - memberId: {}", memberId);
                return new CustomException(ErrorCode.ONBOARDING_NOT_STARTED);
            });
    }
    
    /**
     * 세션 ID 생성
     */
    private String generateSessionId(Long memberId) {
        return String.format("ONB-%d-%s", memberId, UUID.randomUUID().toString().substring(0, 8));
    }
}
