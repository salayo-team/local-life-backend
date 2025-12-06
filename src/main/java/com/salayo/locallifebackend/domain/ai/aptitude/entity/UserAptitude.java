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

	@Column(name = "aptitude_test_count", nullable = false)
	private Integer aptitudeTestCount;

	@Builder
	public UserAptitude(Member member, AptitudeType aptitudeType, Integer aptitudeTestCount) {
		this.member = member;
		this.aptitudeType = aptitudeType;
		this.aptitudeTestCount = (aptitudeTestCount != null) ? aptitudeTestCount : 0;
	}

	public void updateAptitude(AptitudeType aptitudeType) {
		this.aptitudeType = aptitudeType;
		this.aptitudeTestCount++;
	}
}
