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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_aptitudes")
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

	@Column(name = "test_step")
	private Integer testStep;

	@Column(name = "test_count", nullable = false)
	private Integer testCount;

	@Builder
	public UserAptitude(Member member, AptitudeType aptitudeType, Integer testStep, Integer testCount) {
		this.member = member;
		this.aptitudeType = aptitudeType;
		this.testStep = testStep;
		this.testCount = (testCount != null) ? testCount : 0;
	}

	public void updateAptitude(AptitudeType aptitudeType) {
		this.aptitudeType = aptitudeType;
		this.testCount++;
	}

	public void updateTestStep(Integer testStep) {
		this.testStep = testStep;
	}
}
