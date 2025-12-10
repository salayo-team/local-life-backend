package com.salayo.locallifebackend.domain.program.repository;


import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.salayo.locallifebackend.domain.program.dto.ProgramSearchRequestDto;
import com.salayo.locallifebackend.domain.program.entity.Program;
import com.salayo.locallifebackend.domain.program.entity.QProgram;
import com.salayo.locallifebackend.domain.program.enums.ProgramStatus;
import com.salayo.locallifebackend.domain.program.enums.SortType;
import com.salayo.locallifebackend.global.enums.DeletedStatus;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class ProgramRepositoryCustomImpl implements ProgramRepositoryCustom {

	private final JPAQueryFactory jpaQueryFactory;

	public ProgramRepositoryCustomImpl(JPAQueryFactory jpaQueryFactory) {
		this.jpaQueryFactory = jpaQueryFactory;
	}

	@Override
	public Page<Program> searchPrograms(ProgramSearchRequestDto requestDto) {

		QProgram program = QProgram.program;

		List<BooleanExpression> conditions = new ArrayList<>();
		conditions.add(program.deletedStatus.eq(DeletedStatus.DISPLAYED));
		conditions.add(program.programStatus.eq(ProgramStatus.REGISTERED));

		if (requestDto.getRegionIds() != null && !requestDto.getRegionIds().isEmpty()) {
			conditions.add(program.regionCategory.id.in(requestDto.getRegionIds()));
		}
		if (requestDto.getAptitudeIds() != null && !requestDto.getAptitudeIds().isEmpty()) {
			conditions.add(program.aptitudeCategory.id.in(requestDto.getAptitudeIds()));
		}
		if (requestDto.getIsLocalSpecialized() != null) {
			conditions.add(program.isLocalSpecialized.eq(requestDto.getIsLocalSpecialized()));
		}

		NumberTemplate<BigDecimal> effectivePrice = Expressions.numberTemplate(
			BigDecimal.class,
			"COALESCE({0},{1})",
			program.finalPrice,
			program.price
		);
		if (requestDto.getMinPrice() != null) {
			conditions.add(effectivePrice.goe(requestDto.getMinPrice()));
		}
		if (requestDto.getMaxPrice() != null) {
			conditions.add(effectivePrice.loe(requestDto.getMaxPrice()));
		}

		int page = 0;
		int size = ProgramSearchRequestDto.DEFAULT_SIZE;
		if (requestDto.getPage() != null && requestDto.getPage() >= 0) {
			page = requestDto.getPage();
		}
		if (requestDto.getSize() != null && requestDto.getSize() > 0) {
			size = requestDto.getSize();
		}
		Pageable pageable = PageRequest.of(page, size);

		OrderSpecifier<? extends Comparable<?>> sortOrder = getSortOrder(requestDto.getSort(), program);

		boolean hasKeyword = hasText(requestDto.getKeyword());
		if (!hasKeyword) {
			return defaultSearch(program, conditions, sortOrder, pageable);
		}

		String fullTextKeyword = buildFullTextKeyword(requestDto.getKeyword());

		NumberTemplate<Double> score = Expressions.numberTemplate(
			Double.class,
			"function('match_title_business', {0}, {1}, {2})",
			program.title,
			program.businessName,
			fullTextKeyword
		);

		BooleanExpression[] predicates = conditions.toArray(BooleanExpression[]::new);

		List<Program> content = jpaQueryFactory
			.selectFrom(program)
			.where(predicates)
			.where(score.gt(0d))
			.orderBy(score.desc(), sortOrder)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		Long total = jpaQueryFactory.
			select(program.count())
			.from(program)
			.where(predicates)
			.where(score.gt(0d))
			.fetchOne();

		long totalCount = 0L;
		if (total != null) {
			totalCount = total;
		}

		return new PageImpl<>(content, pageable, totalCount);
	}

	private boolean hasText(String value) {

		if (value == null || value.isEmpty()) {
			return false;
		}

		return true;
	}

	/**
	 * 검색 키워드가 없을 경우
	 * - 기본 필터링 조건만으로 조회
	 */
	private Page<Program> defaultSearch(QProgram program, List<BooleanExpression> conditions,
		OrderSpecifier<? extends Comparable<?>> sortOrder, Pageable pageable) {

		BooleanExpression[] predicates = conditions.toArray(BooleanExpression[]::new);

		List<Program> content = jpaQueryFactory
			.selectFrom(program)
			.where(predicates)
			.orderBy(sortOrder)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		Long total = jpaQueryFactory
			.select(program.count())
			.from(program)
			.where(predicates)
			.fetchOne();

		long totalCount = 0L;
		if (total != null) {
			totalCount = total;
		}

		return new PageImpl<>(content, pageable, totalCount);
	}

	/**
	 * 정렬 조건 설정
	 */
	private OrderSpecifier<? extends Comparable<?>> getSortOrder(SortType sortType, QProgram program) {

		if (sortType == null) {
			return program.createdAt.desc();
		}

		return switch (sortType) {
			case LATEST -> program.createdAt.desc();
			case PRICE_ASC -> program.finalPrice.asc();
			case PRICE_DESC -> program.finalPrice.desc();
			case REVIEW_COUNT_DESC -> program.reviewCount.desc();
			case RATING_DESC -> program.averageRating.desc();
			default -> throw new CustomException(ErrorCode.INVALID_SORT_TYPE);
		};
	}

	/**
	 * FULLTEXT SEARCH 키워드
	 */
	private String buildFullTextKeyword(String keyword) {

		if (!hasText(keyword)) {
			throw new CustomException(ErrorCode.INVALID_SEARCH_KEYWORD);
		}

		List<String> words = List.of(keyword.split("\\s+"));

		String fulltextQuery = words.stream()
			.map(word -> "+" + word + "*")
			.collect(Collectors.joining(" "));

		return fulltextQuery;
	}

}
