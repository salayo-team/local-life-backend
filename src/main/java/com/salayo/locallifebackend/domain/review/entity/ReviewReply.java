package com.salayo.locallifebackend.domain.review.entity;

import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.review.enums.ReviewStatus;
import com.salayo.locallifebackend.global.entity.BaseEntity;
import com.salayo.locallifebackend.global.enums.DeletedStatus;
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
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review_replies")
@Getter
@NoArgsConstructor
public class ReviewReply extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "reply_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "review_id", nullable = false)
	private Review review;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "local_creator_id", nullable = false)
	private Member member;

	@Column(columnDefinition = "TEXT", nullable = false)
	private String content;

	@Enumerated(EnumType.STRING)
	@Column(name = "deleted_status", nullable = false)
	private DeletedStatus deletedStatus = DeletedStatus.DISPLAYED;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Builder
	public ReviewReply(Review review, Member member, String content) {
		this.review = review;
		this.member = member;
		this.content = content;
		this.deletedStatus = DeletedStatus.DISPLAYED;
	}

	public void deleteReply() {
		this.deletedStatus = DeletedStatus.DELETED;
		this.deletedAt = LocalDateTime.now();
	}
}
