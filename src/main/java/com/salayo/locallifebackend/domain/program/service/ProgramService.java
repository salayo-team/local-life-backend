package com.salayo.locallifebackend.domain.program.service;

import com.salayo.locallifebackend.domain.category.entity.AptitudeCategory;
import com.salayo.locallifebackend.domain.category.entity.RegionCategory;
import com.salayo.locallifebackend.domain.category.repository.AptitudeCategoryRepository;
import com.salayo.locallifebackend.domain.category.repository.RegionCategoryRepository;
import com.salayo.locallifebackend.domain.program.dto.ProgramCreateRequestDto;
import com.salayo.locallifebackend.domain.program.dto.ProgramCreateResponseDto;
import com.salayo.locallifebackend.domain.program.entity.Program;
import com.salayo.locallifebackend.domain.program.enums.ProgramStatus;
import com.salayo.locallifebackend.domain.program.repository.ProgramRepository;
import com.salayo.locallifebackend.domain.user.entity.Member;
import com.salayo.locallifebackend.domain.user.repository.MemberRepository;
import com.salayo.locallifebackend.global.enums.DeletedStatus;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProgramService {

	private final AptitudeCategoryRepository aptitudeCategoryRepository;
	private final RegionCategoryRepository regionCategoryRepository;
	private final ProgramRepository programRepository;
	private final MemberRepository memberRepository;

	public ProgramService(AptitudeCategoryRepository aptitudeCategoryRepository, RegionCategoryRepository regionCategoryRepository,
		ProgramRepository programRepository,
		MemberRepository memberRepository) {
		this.aptitudeCategoryRepository = aptitudeCategoryRepository;
		this.regionCategoryRepository = regionCategoryRepository;
		this.programRepository = programRepository;
		this.memberRepository = memberRepository;
	}

	/**
	 * 체험 프로그램 생성
	 * - TODO : 파일 로직 추가, 하드코드 값 수정
	 */
	@Transactional
	public ProgramCreateResponseDto createProgram(long userId, ProgramCreateRequestDto requestDto) {

		//TODO : 유저 롤 검증
		Member member = memberRepository.findByIdOrElseThrow();

		//TODO : 유저 상호명 조회 - 로컬 크리에이터 정보

		//적성 카테고리 조회
		Long aptitudeCategoryId = requestDto.getAptitudeCategoryId();
		AptitudeCategory aptitudeCategory = aptitudeCategoryRepository.findByIdOrElseThrow(aptitudeCategoryId);

		//지역 카테고리 조회
		Long regionCategoryId = requestDto.getRegionCategoryId();
		RegionCategory regionCategory = regionCategoryRepository.findByIdOrElseThrow(regionCategoryId);

		//할인 가격
		BigDecimal price = requestDto.getPrice();
		BigDecimal percent = requestDto.getPercent();

		BigDecimal discountedPrice = null;

		if (percent != null) {
			BigDecimal discountRate = BigDecimal.ONE.subtract(percent.divide(BigDecimal.valueOf(100)));
			discountedPrice = price.multiply(discountRate);
		}
		
		//시작 & 종료 날짜 - 현재 날짜로부터 일주일 뒤부터 설정 가능

		//모집기간 - 체험 프로그램 생성한 날짜부터 프로그램 시작일 2일 전까지

		//체험 프로그램 생성
		Program program = Program.builder()
			.aptitudeCategory(aptitudeCategory)
			.regionCategory(regionCategory)
			.businessName("상호명")
			.title(requestDto.getTitle())
			.description(requestDto.getDescription())
			.location(requestDto.getLocation())
			.price(requestDto.getPrice())
			.percent(requestDto.getPercent())
			.discountedPrice(discountedPrice)
			.capacity(requestDto.getCapacity())
			.recruitmentPeriod()
			.startDate()
			.endDate()
			.startTime(requestDto.getStartTime())
			.endTime(requestDto.getEndTime())
			.count(0)
			.programStatus(ProgramStatus.ACTIVE)
			.deletedStatus(DeletedStatus.DISPLAYED)
			.build();

		//저장
		programRepository.save(program);

		return ProgramCreateResponseDto.builder().build();
	}

}
