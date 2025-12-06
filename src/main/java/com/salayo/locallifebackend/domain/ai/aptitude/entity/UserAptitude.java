package com.salayo.locallifebackend.domain.ai.aptitude.entity;

import com.salayo.locallifebackend.domain.ai.aptitude.enums.AptitudeType;
import com.salayo.locallifebackend.domain.member.entity.Member;
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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_aptitudes", uniqueConstraints = {
	@UniqueConstraint(name = "UK_user_aptitude_member_id", columnNames = "member_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAptitude extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_aptitude_id")
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@Enumerated(EnumType.STRING)
	@Column(name = "aptitude_code", nullable = false)
	private AptitudeType aptitudeType;

	@Column(name = "aptitude_test_step")
	private Integer aptitudeTestStep;

	@Column(name = "aptitude_test_count", nullable = false)
	private Integer aptitudeTestCount;

	@Column(name = "mypage_test_count", nullable = false)
	private Integer mypageTestCount;

	@Column(name = "is_onboarding_completed", nullable = false)
	private Boolean isOnboardingCompleted;
	
	@Column(name = "last_partial_step")
	private Integer lastPartialStep; // 마지막 부분 저장 단계
	
	@Column(name = "partial_session_id", length = 100)
	private String partialSessionId; // 부분 저장된 세션 ID

	@Builder
	public UserAptitude(Member member, AptitudeType aptitudeType, Integer aptitudeTestStep,
			Integer aptitudeTestCount, Integer mypageTestCount, Boolean isOnboardingCompleted,
			Integer lastPartialStep, String partialSessionId) {
		this.member = member;
		this.aptitudeType = aptitudeType;
		this.aptitudeTestStep = aptitudeTestStep;
		this.aptitudeTestCount = (aptitudeTestCount != null) ? aptitudeTestCount : 0;
		this.mypageTestCount = (mypageTestCount != null) ? mypageTestCount : 0;
		this.isOnboardingCompleted = (isOnboardingCompleted != null) ? isOnboardingCompleted : false;
		this.lastPartialStep = lastPartialStep;
		this.partialSessionId = partialSessionId;
	}

	public static UserAptitude createNew(Member member) {
		return UserAptitude.builder()
			.member(member)
			.aptitudeType(AptitudeType.PENDING)
			.aptitudeTestStep(0)
			.aptitudeTestCount(0)
			.mypageTestCount(0)
			.isOnboardingCompleted(false)
			.lastPartialStep(null)
			.partialSessionId(null)
			.build();
	}

	public void updateAptitudeFromOnboarding(AptitudeType aptitudeType) {
		this.aptitudeType = aptitudeType;
		this.isOnboardingCompleted = true;
		this.aptitudeTestCount++;
	}

	public void updateAptitudeFromMypage(AptitudeType aptitudeType) {
		this.aptitudeType = aptitudeType;
		this.aptitudeTestCount++;
	}

	public void incrementMypageTestCount() {
		this.mypageTestCount++;
	}
	
	// 부분 저장 업데이트
	public void updatePartialProgress(Integer step, String sessionId) {
		this.lastPartialStep = step;
		this.partialSessionId = sessionId;
	}
	
	// 부분 저장 초기화
	public void clearPartialProgress() {
		this.lastPartialStep = null;
		this.partialSessionId = null;
	}
}
