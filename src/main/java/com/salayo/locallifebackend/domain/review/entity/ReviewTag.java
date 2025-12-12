package com.salayo.locallifebackend.domain.review.entity;

import com.salayo.locallifebackend.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review_tag")
@Getter
@NoArgsConstructor
public class ReviewTag extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "review_tag_id")
	private Long id;

	@Column(name = "review_tag_name", nullable = false, unique = true, length = 50)
	private String tagName;

	@Column(name = "display_order", nullable = false)
	private int displayOrder;

	@Column(name = "tag_is_active", nullable = false)
	private boolean tagIsActive = true;

	@Builder
	public ReviewTag(String tagName, int displayOrder, boolean isActive) {
		this.tagName = tagName;
		this.displayOrder = displayOrder;
		this.tagIsActive = isActive;
	}
}
