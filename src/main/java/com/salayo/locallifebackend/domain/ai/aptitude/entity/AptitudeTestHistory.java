package com.salayo.locallifebackend.domain.ai.aptitude.entity;

import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "aptitude_test_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AptitudeTestHistory extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "history_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@Column(nullable = false)
	private Integer step;

	@Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
	private String questionText;

	@Column(name = "user_response", columnDefinition = "TEXT", nullable = false)
	private String userResponse;

	@Column(name = "ai_response", columnDefinition = "TEXT")
	private String aiResponse;

	@Builder
	public AptitudeTestHistory(Member member, Integer step, String questionText, String userResponse, String aiResponse) {
		this.member = member;
		this.step = step;
		this.questionText = questionText;
		this.userResponse = userResponse;
		this.aiResponse = aiResponse;
	}
}
