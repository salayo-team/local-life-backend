package com.salayo.locallifebackend.domain.review.entity;

import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.program.entity.Program;
import com.salayo.locallifebackend.domain.reservation.entity.Reservation;
import com.salayo.locallifebackend.global.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "review")
@Getter
@NoArgsConstructor
@SQLRestriction("status = 'DISPLAYED'")
public class Review extends SoftDeletableEntity {

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

	@OneToMany(mappedBy = "review", fetch = FetchType.LAZY)
	private List<ReviewReply> replies = new ArrayList<>();

	@Builder
	public Review(Member member, Program program, Reservation reservation, String content, List<ReviewReply> replies) {
		this.member = member;
		this.program = program;
		this.reservation = reservation;
		this.content = content;
		this.replies = replies;
	}

	public void updateContent(String content) {
		this.content = content;
	}

	/**
	 * 리뷰와 연관된 답글까지 함께 Soft Delete
	 */
	@Override
	public void softDelete() {
		super.softDelete();

		Optional.ofNullable(this.replies)
			.ifPresent(theReplies -> theReplies.forEach(ReviewReply::softDelete));
	}
}