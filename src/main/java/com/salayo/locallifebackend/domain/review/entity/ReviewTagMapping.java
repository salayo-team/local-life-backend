package com.salayo.locallifebackend.domain.review.entity;

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
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review_tag_mapping", uniqueConstraints = {
	@UniqueConstraint(columnNames = {"review_id", "review_tag_id"})
})
@Getter
@NoArgsConstructor
public class ReviewTagMapping extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "review_tag_mapping_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "review_id", nullable = false)
	private Review review;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "review_tag_id", nullable = false)
	private ReviewTag reviewTag;

	@Builder
	public ReviewTagMapping(Review review, ReviewTag reviewTag) {
		this.review = review;
		this.reviewTag = reviewTag;
	}
}
