package com.salayo.locallifebackend.domain.onboarding.entity;

import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.onboarding.enums.OnboardingStep;
import com.salayo.locallifebackend.domain.onboarding.enums.RegionType;
import com.salayo.locallifebackend.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "onboarding_progress")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OnboardingProgress extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "onboarding_progress_id")
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "current_step", nullable = false)
    private OnboardingStep currentStep;
    
    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted;
    
    @Column(name = "knows_aptitude")
    private Boolean knowsAptitude; // 적성 인지 여부

    @Enumerated(EnumType.STRING) // DB에는 enum의 이름(URBAN, BALANCED, NATURE)이 문자열로 저장
    @Column(name = "regionType")
    private RegionType regionType;
    
    @Column(name = "session_id")
    private String sessionId; // 온보딩 세션 ID
    
    @Builder
    public OnboardingProgress(Member member, OnboardingStep currentStep, 
                             boolean isCompleted, Boolean knowsAptitude,
                             RegionType regionType, String sessionId) {
        this.member = member;
        this.currentStep = currentStep != null ? currentStep : OnboardingStep.MEMBER_INFO;
        this.isCompleted = isCompleted;
        this.knowsAptitude = knowsAptitude;
        this.regionType = regionType;
        this.sessionId = sessionId;
    }
    
    /**
     * 다음 단계로 진행
     */
    public void moveToNextStep(Boolean knowsAptitude) {
        this.currentStep = this.currentStep.getNextStep(knowsAptitude);
        if (this.currentStep == OnboardingStep.COMPLETED) {
            this.isCompleted = true;
        }
    }
    
    /**
     * 선호 지역 설정
     */
    public void updateRegion(RegionType regionType) {
        this.regionType = regionType;
    }
    
    /**
     * 적성 인지 여부 설정
     */
    public void updateKnowsAptitude(Boolean knows) {
        this.knowsAptitude = knows;
    }
    
    /**
     * 온보딩 완료 처리
     */
    public void complete() {
        this.currentStep = OnboardingStep.COMPLETED;
        this.isCompleted = true;
    }
}
