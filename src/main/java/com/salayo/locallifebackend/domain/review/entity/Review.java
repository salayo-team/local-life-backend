package com.salayo.locallifebackend.domain.review.entity;

import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.program.entity.Program;
import com.salayo.locallifebackend.domain.reservation.entity.Reservation;
import com.salayo.locallifebackend.domain.review.enums.ReviewStatus;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review")
@Getter
@NoArgsConstructor
public class Review extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "review_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "program_id", nullable = false)
	private Program program;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reservation_id", nullable = false)
	private Reservation reservation;

	@Column(columnDefinition = "TEXT", nullable = false)
	private String content;

	@Enumerated(EnumType.STRING)
	@Column(name = "review_status", nullable = false)
	private ReviewStatus reviewStatus = ReviewStatus.DISPLAYED;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@OneToMany(mappedBy = "review", fetch = FetchType.LAZY)
	private List<ReviewReply> replies = new ArrayList<>();

	@Builder
	public Review(Member member, Program program, Reservation reservation, String content) {
		this.member = member;
		this.program = program;
		this.reservation = reservation;
		this.content = content;
		this.reviewStatus = ReviewStatus.DISPLAYED;
	}

	public void updateContent(String content) {
		this.content = content;
	}

	public void deleteReview() {
		this.reviewStatus = ReviewStatus.DELETED;
		this.deletedAt = LocalDateTime.now();
	}

	public boolean isModified() {
		return getUpdatedAt() != null && getUpdatedAt().isAfter(getCreatedAt());
	}
}
