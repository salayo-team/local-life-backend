package com.salayo.locallifebackend.domain.review.entity;

import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.global.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "review_replies")
@Getter
@NoArgsConstructor
@SQLRestriction("deleted_status = 'DISPLAYED'")
public class ReviewReply extends SoftDeletableEntity {

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

	@Column(name = "reply_content", columnDefinition = "TEXT", nullable = false)
	private String content;

	@Builder
	public ReviewReply(Review review, Member member, String content) {
		this.review = review;
		this.member = member;
		this.content = content;
	}
}
