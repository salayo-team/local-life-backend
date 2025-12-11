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
	
	@Enumerated(EnumType.STRING)
	@Column(name = "analyzed_aptitude_type", length = 30)
	private AptitudeType analyzedAptitudeType;
	
	@Column(name = "confidence_score")
	private Double confidenceScore;
	
	@Column(name = "session_id", length = 100)
	private String sessionId;
	
	@Column(name = "is_completed", nullable = false)
	private boolean isCompleted = false;
	
	@Column(name = "is_partial_save", nullable = false)
	private boolean isPartialSave = false;
	
	@Column(name = "last_completed_step")
	private Integer lastCompletedStep;

	@Builder
	public AptitudeTestHistory(Member member, Integer step, String questionText, 
							   String userResponse, String aiResponse, AptitudeType analyzedAptitudeType,
							   Double confidenceScore, String sessionId, Boolean isCompleted,
							   Boolean isPartialSave, Integer lastCompletedStep) {
		this.member = member;
		this.step = step;
		this.questionText = questionText;
		this.userResponse = userResponse;
		this.aiResponse = aiResponse;
		this.analyzedAptitudeType = analyzedAptitudeType;
		this.confidenceScore = confidenceScore;
		this.sessionId = sessionId;
		this.isCompleted = isCompleted != null ? isCompleted : false;
		this.isPartialSave = isPartialSave != null ? isPartialSave : false;
		this.lastCompletedStep = lastCompletedStep;
	}
}
