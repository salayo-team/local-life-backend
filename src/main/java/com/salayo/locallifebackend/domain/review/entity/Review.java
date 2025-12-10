package com.salayo.locallifebackend.domain.review.entity;

import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.program.entity.Program;
import com.salayo.locallifebackend.domain.reservation.entity.Reservation;
import com.salayo.locallifebackend.global.entity.SoftDeletableEntity;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "review")
@Getter
@NoArgsConstructor
@SQLRestriction("deleted_status = 'DISPLAYED'")
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

	@OneToMany(mappedBy = "review", fetch = FetchType.LAZY)
	@BatchSize(size = 100)
	private List<ReviewReply> replies = new ArrayList<>();

	@Column(name = "content", nullable = false)
	private String content;

	@Column(name = "review_rating", nullable = false, precision = 2, scale = 1)
	private BigDecimal reviewRating;

	@Column(name = "view_count", nullable = false)
	private int viewCount = 0;

	@Column(name = "like_count", nullable = false)
	private int likeCount = 0;

	@Builder
	public Review(Member member, Program program, Reservation reservation,
		String content, BigDecimal rating) {

		validateRating(rating);

		this.member = member;
		this.program = program;
		this.reservation = reservation;
		this.content = content;
		this.reviewRating = rating;
	}

	public void updateContent(String content) {
		this.content = content;
	}

	public void updateRating(BigDecimal newRating) {
		validateRating(newRating);
		this.reviewRating = newRating;
	}

	public void validateRating(BigDecimal reviewRating) {
		if (reviewRating == null) {
			throw new CustomException(ErrorCode.RATING_CANNOT_BE_NULL);
		}
		if (reviewRating.compareTo(BigDecimal.ZERO) < 0 || reviewRating.compareTo(new BigDecimal("5.0")) > 0) {
			throw new CustomException(ErrorCode.INVALID_RATING_RANGE);
		}
		if (reviewRating.multiply(new BigDecimal("2")).stripTrailingZeros().scale() > 0) {
			throw new CustomException(ErrorCode.INVALID_RATING_UNIT);
		}
	}

	public void incrementViewCount() {
		this.viewCount++;
	}

	public void incrementLikeCount() {
		this.likeCount++;
	}

	public void decrementLikeCount() {
		if (this.likeCount > 0) {
			this.likeCount--;
		}
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