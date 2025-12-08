package com.salayo.locallifebackend.domain.onboarding.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.salayo.locallifebackend.domain.category.entity.AptitudeCategory;
import com.salayo.locallifebackend.domain.category.entity.RegionCategory;
import com.salayo.locallifebackend.domain.program.entity.Program;
import com.salayo.locallifebackend.domain.program.entity.QProgram;
import com.salayo.locallifebackend.domain.program.enums.ProgramStatus;
import com.salayo.locallifebackend.global.enums.DeletedStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 온보딩 프로그램 추천 Repository 구현체 QueryDSL을 사용한 동적 쿼리 구현
 */
@Slf4j
@Repository
public class OnboardingProgramRepositoryImpl implements OnboardingProgramRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public OnboardingProgramRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
		this.jpaQueryFactory = jpaQueryFactory;
	}

	@Override
	public List<Program> findRecommendedPrograms(
		AptitudeCategory aptitudeCategory,
		List<RegionCategory> regionCategories,
		ProgramStatus status,
		DeletedStatus deletedStatus,
		LocalDate currentDate
	) {
		QProgram program = QProgram.program;

		NumberTemplate<Double> random = Expressions.numberTemplate(Double.class, "function('RAND')");

		BooleanExpression conditions = program.aptitudeCategory.eq(aptitudeCategory)
			.and(program.regionCategory.in(regionCategories))
			.and(program.programStatus.eq(status))
			.and(program.deletedStatus.eq(deletedStatus))
			.and(program.startDate.loe(currentDate))
			.and(program.endDate.goe(currentDate));

		List<Program> programs = jpaQueryFactory.selectFrom(program)
			.where(conditions)
			.orderBy(random.asc())
			.limit(6)
			.fetch();

		log.debug("추천 프로그램 조회 (랜덤) - 적성: {}, 지역 수: {}, 조회 결과: {}개",
			aptitudeCategory.getAptitudeCode(), regionCategories.size(), programs.size());

		return programs;
	}
}
