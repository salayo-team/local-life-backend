package com.salayo.locallifebackend.domain.program.repository;


import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.salayo.locallifebackend.domain.program.dto.ProgramSearchRequestDto;
import com.salayo.locallifebackend.domain.program.entity.Program;
import com.salayo.locallifebackend.domain.program.entity.QProgram;
import com.salayo.locallifebackend.domain.program.enums.SortType;
import com.salayo.locallifebackend.global.enums.DeletedStatus;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import java.util.ArrayList;
import java.util.List;
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

		if (requestDto.getRegionIds() != null && !requestDto.getRegionIds().isEmpty()) {
			conditions.add(program.regionCategory.id.in(requestDto.getRegionIds()));
		}

		if (requestDto.getAptitudeIds() != null && !requestDto.getAptitudeIds().isEmpty()) {
			conditions.add(program.aptitudeCategory.id.in(requestDto.getAptitudeIds()));
		}

		OrderSpecifier<?> sortOrder = getSortOrder(requestDto.getSort(), program);

		List<Program> content = jpaQueryFactory.selectFrom(program)
			.where(conditions.toArray(
				BooleanExpression[]::new))
			.orderBy(sortOrder)
			.offset((long) requestDto.getPage() * requestDto.getSize())
			.limit(requestDto.getSize())
			.fetch();

		Long total = jpaQueryFactory.select(program.count())
			.from(program)
			.where(conditions.toArray(BooleanExpression[]::new))
			.fetchOne();

		long totalCount = 0;
		if (total != null) {
			totalCount = total;
		}

		Pageable pageable = PageRequest.of(requestDto.getPage(), requestDto.getSize());

		return new PageImpl<>(content, pageable, totalCount);
	}

	/**
	 * 정렬 조건 설정
	 */
	private OrderSpecifier<?> getSortOrder(SortType sortType, QProgram program) {
		OrderSpecifier<?> orderSpecifier;

		if (sortType == null) {
			throw new CustomException(ErrorCode.INVALID_SORT_TYPE);
		}

		switch (sortType) {
			case LATEST -> orderSpecifier = program.createdAt.desc();
			case PRICE_ASC -> orderSpecifier = program.finalPrice.asc();
			case PRICE_DESC -> orderSpecifier = program.finalPrice.desc();
			default -> throw new CustomException(ErrorCode.INVALID_SORT_TYPE);
		}

		return orderSpecifier;
	}
}
