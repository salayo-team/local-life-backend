package com.salayo.locallifebackend.domain.ai.aptitude.entity;

import com.salayo.locallifebackend.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "aptitude_test_progress")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AptitudeTestProgress extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "aptitude_test_progress_id")
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_aptitude_id", nullable = false, unique = true)
	private UserAptitude userAptitude;

	@Column(name = "aptitude_test_step")
	private Integer aptitudeTestStep;

	@Column(name = "mypage_test_count", nullable = false)
	private Integer mypageTestCount;

	@Column(name = "last_partial_step")
	private Integer lastPartialStep;

	@Column(name = "partial_session_id", length = 100)
	private String partialSessionId;

	@Builder
	public AptitudeTestProgress(UserAptitude userAptitude, Integer aptitudeTestStep,
		Integer mypageTestCount, Integer lastPartialStep, String partialSessionId) {
		this.userAptitude = userAptitude;
		this.aptitudeTestStep = aptitudeTestStep;
		this.mypageTestCount = (mypageTestCount != null) ? mypageTestCount : 0;
		this.lastPartialStep = lastPartialStep;
		this.partialSessionId = partialSessionId;
	}

	public void incrementMypageTestCount() {
		this.mypageTestCount++;
	}

	public void updatePartialProgress(Integer step, String sessionId) {
		this.lastPartialStep = step;
		this.partialSessionId = sessionId;
	}

	public void clearPartialProgress() {
		this.lastPartialStep = null;
		this.partialSessionId = null;
	}
}
