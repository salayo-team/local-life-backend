package com.salayo.locallifebackend.domain.review.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.salayo.locallifebackend.domain.program.entity.QProgram;
import com.salayo.locallifebackend.domain.review.dto.ReviewSearchRequestDto;
import com.salayo.locallifebackend.domain.review.entity.QReview;
import com.salayo.locallifebackend.domain.review.entity.QReviewTagMapping;
import com.salayo.locallifebackend.domain.review.entity.Review;
import com.salayo.locallifebackend.domain.review.enums.ReviewSortType;
import com.salayo.locallifebackend.global.enums.DeletedStatus;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class ReviewCustomRepositoryImpl implements ReviewCustomRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public ReviewCustomRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
		this.jpaQueryFactory = jpaQueryFactory;
	}

	@Override
	public Page<Review> searchReviews(ReviewSearchRequestDto requestDto) {
		QReview review = QReview.review;
		QProgram program = QProgram.program;
		QReviewTagMapping tagMapping = QReviewTagMapping.reviewTagMapping;

		List<BooleanExpression> conditions = new ArrayList<>();
		conditions.add(review.deletedStatus.eq(DeletedStatus.DISPLAYED));

		if (requestDto.getTagIds() != null && !requestDto.getTagIds().isEmpty()) {
			List<Long> reviewIdsWithTags = jpaQueryFactory
				.select(tagMapping.review.id)
				.from(tagMapping)
				.where(tagMapping.reviewTag.id.in(requestDto.getTagIds()))
				.fetch();

			if (!reviewIdsWithTags.isEmpty()) {
				conditions.add(review.id.in(reviewIdsWithTags));
			} else {
				return new PageImpl<>(new ArrayList<>(), PageRequest.of(requestDto.getPage(), requestDto.getSize()), 0);
			}
		}

		if (requestDto.getAptitudeIds() != null && !requestDto.getAptitudeIds().isEmpty()) {
			conditions.add(review.program.aptitudeCategory.id.in(requestDto.getAptitudeIds()));
		}

		if (requestDto.getRegionIds() != null && !requestDto.getRegionIds().isEmpty()) {
			conditions.add(review.program.regionCategory.id.in(requestDto.getRegionIds()));
		}

		if (requestDto.getProgramGroupId() != null) {
			conditions.add(review.program.programGroup.id.eq(requestDto.getProgramGroupId()));
		}

		if (requestDto.getKeyword() != null && !requestDto.getKeyword().isBlank()) {
			String keyword = "%" + requestDto.getKeyword().trim() + "%";
			conditions.add(
				review.member.nickname.likeIgnoreCase(keyword)
					.or(review.content.likeIgnoreCase(keyword))
			);
		}

		OrderSpecifier<?> sortOrder = getSortOrder(requestDto.getSort(), review);

		List<Review> content = jpaQueryFactory
			.selectFrom(review)
			.join(review.program, program).fetchJoin()
			.where(conditions.toArray(BooleanExpression[]::new))
			.orderBy(sortOrder)
			.offset((long) requestDto.getPage() * requestDto.getSize())
			.limit(requestDto.getSize())
			.fetch();

		Long total = jpaQueryFactory
			.select(review.count())
			.from(review)
			.where(conditions.toArray(BooleanExpression[]::new))
			.fetchOne();

		long totalCount = (total != null) ? total : 0;
		Pageable pageable = PageRequest.of(requestDto.getPage(), requestDto.getSize());

		return new PageImpl<>(content, pageable, totalCount);
	}

	private OrderSpecifier<?> getSortOrder(ReviewSortType sortType, QReview review) {
		if (sortType == null) {
			return review.createdAt.desc();
		}

		return switch (sortType) {
			case LATEST -> review.createdAt.desc();
			case OLDEST -> review.createdAt.asc();
			case RATING_HIGH -> review.reviewRating.desc();
			case RATING_LOW -> review.reviewRating.asc();
			case LIKE_COUNT -> review.likeCount.desc();
			case VIEW_COUNT -> review.viewCount.desc();
		};
	}
}
