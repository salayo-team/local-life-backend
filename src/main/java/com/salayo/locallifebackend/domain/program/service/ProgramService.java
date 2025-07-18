package com.salayo.locallifebackend.domain.program.service;


import com.salayo.locallifebackend.domain.category.entity.AptitudeCategory;
import com.salayo.locallifebackend.domain.category.entity.RegionCategory;
import com.salayo.locallifebackend.domain.category.repository.AptitudeCategoryRepository;
import com.salayo.locallifebackend.domain.category.repository.RegionCategoryRepository;
import com.salayo.locallifebackend.domain.file.repository.FileRepository;
import com.salayo.locallifebackend.domain.localcreator.entity.LocalCreator;
import com.salayo.locallifebackend.domain.localcreator.enums.CreatorStatus;
import com.salayo.locallifebackend.domain.localcreator.repository.LocalCreatorRepository;
import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.member.repository.MemberRepository;
import com.salayo.locallifebackend.domain.program.dto.ProgramCreateRequestDto;
import com.salayo.locallifebackend.domain.program.dto.ProgramCreateResponseDto;
import com.salayo.locallifebackend.domain.program.entity.Program;
import com.salayo.locallifebackend.domain.program.entity.ProgramDay;
import com.salayo.locallifebackend.domain.program.entity.ProgramScheduleTime;
import com.salayo.locallifebackend.domain.program.enums.LocalSpecialized;
import com.salayo.locallifebackend.domain.program.enums.ProgramStatus;
import com.salayo.locallifebackend.domain.program.repository.ProgramRepository;
import com.salayo.locallifebackend.global.enums.DeletedStatus;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.stereotype.Service;


@Service
public class ProgramService {

	private final AptitudeCategoryRepository aptitudeCategoryRepository;
	private final RegionCategoryRepository regionCategoryRepository;
	private final ProgramRepository programRepository;
	private final MemberRepository memberRepository;
	private final LocalCreatorRepository localCreatorRepository;
	private final FileRepository fileRepository;

	public ProgramService(AptitudeCategoryRepository aptitudeCategoryRepository, RegionCategoryRepository regionCategoryRepository,
		ProgramRepository programRepository, MemberRepository memberRepository, LocalCreatorRepository localCreatorRepository,
		FileRepository fileRepository) {
		this.aptitudeCategoryRepository = aptitudeCategoryRepository;
		this.regionCategoryRepository = regionCategoryRepository;
		this.programRepository = programRepository;
		this.memberRepository = memberRepository;
		this.localCreatorRepository = localCreatorRepository;
		this.fileRepository = fileRepository;
	}

	/**
	 * 체험 프로그램 생성 메서드
	 * - TODO : file 로직 추가
	 * - TODO : 동일한 유저가 중복되는 스케줄 타임 생성시 예외처리
	 */
	public ProgramCreateResponseDto createProgram(long memberId, @Valid ProgramCreateRequestDto requestDto) {

		Member member = memberRepository.findByIdOrElseThrow(memberId);

		LocalCreator localCreator = localCreatorRepository
			.findByMemberIdAndCreatorStatus(member.getId(), CreatorStatus.APPROVED)
			.orElseThrow(() -> new CustomException(ErrorCode.LOCAL_CREATOR_NOT_APPROVED));

		String businessName = localCreator.getBusinessName();

		Long aptitudeCategoryId = requestDto.getAptitudeCategoryId();
		AptitudeCategory aptitudeCategory = aptitudeCategoryRepository.findByIdOrElseThrow(aptitudeCategoryId);

		Long regionCategoryId = requestDto.getRegionCategoryId();
		RegionCategory regionCategory = regionCategoryRepository.findByIdOrElseThrow(regionCategoryId);

		BigDecimal price = requestDto.getPrice();
		BigDecimal percent = requestDto.getPercent();

		if (price.compareTo(BigDecimal.ONE) < 0 || price.compareTo(new BigDecimal("5000000")) > 0) {
			throw new CustomException(ErrorCode.INVALID_PRICE_RANGE);
		}

		BigDecimal discountedPrice = null;

		if (percent != null) {
			BigDecimal discountRate = BigDecimal.ONE.subtract(percent.divide(BigDecimal.valueOf(100)));
			discountedPrice = price.multiply(discountRate);
		}

		Integer minCapacity = requestDto.getMinCapacity();
		Integer maxCapacity = requestDto.getMaxCapacity();

		if (minCapacity < 1 || maxCapacity > 100 || minCapacity > maxCapacity) {
			throw new CustomException(ErrorCode.INVALID_CAPACITY_RANGE);
		}

		LocalDate startDate = requestDto.getStartDate();
		validateStartDate(startDate);

		LocalDate endDate = requestDto.getEndDate();
		validateEndDate(startDate, endDate);

		Program program = Program.builder()
			.member(member)
			.aptitudeCategory(aptitudeCategory)
			.regionCategory(regionCategory)
			.businessName(businessName)
			.title(requestDto.getTitle())
			.description(requestDto.getDescription())
			.curriculumDescription(requestDto.getCurriculumDescription())
			.location(requestDto.getLocation())
			.price(price)
			.percent(percent)
			.discountedPrice(discountedPrice)
			.minCapacity(minCapacity)
			.maxCapacity(maxCapacity)
			.startDate(startDate)
			.endDate(endDate)
			.count(0)
			.isLocalSpecialized(LocalSpecialized.GENERAL)
			.programStatus(ProgramStatus.REGISTERED)
			.deletedStatus(DeletedStatus.DISPLAYED)
			.build();

		requestDto.getProgramDays().forEach(dayName -> {
			ProgramDay programDay = ProgramDay.builder()
				.dayName(dayName)
				.build();
			program.addProgramDay(programDay);
		});

		requestDto.getProgramScheduleTimes().forEach(scheduleDto -> {
			Integer scheduleDurationHours = scheduleDto.getScheduleDuration();
			LocalTime startTime = scheduleDto.getStartTime();
			LocalTime endTime = startTime.plusHours(scheduleDurationHours);

			ProgramScheduleTime scheduleTime = ProgramScheduleTime.builder()
				.scheduleCount(scheduleDto.getScheduleCount())
				.scheduleDuration(scheduleDurationHours)
				.startTime(startTime)
				.endTime(endTime)
				.build();
			program.addProgramScheduleTime(scheduleTime);
		});

		programRepository.save(program);

		//TODO : 체험 프로그램 스케줄 생성 및 저장

		return ProgramCreateResponseDto.from(program);
	}

	/**
	 * 시작일 검증 메서드
	 * - 시작일 현재 기준 7일 이후, max 1달 이내 검증
	 */
	private void validateStartDate(LocalDate startDate) {

		LocalDate today = LocalDate.now();
		if (startDate.isBefore(today.plusDays(7))) {
			throw new CustomException(ErrorCode.INVALID_START_DATE);
		}
		if (startDate.isAfter(today.plusMonths(1))) {
			throw new CustomException(ErrorCode.INVALID_START_DATE);
		}
	}

	/**
	 * 종료일 검증 메서드
	 * - 종료일 시작일 기준 1주일 후, max 3달 이내 검증
	 */
	private void validateEndDate(LocalDate startDate, LocalDate endDate) {
		if (endDate.isBefore(startDate.plusWeeks(1))) {
			throw new CustomException(ErrorCode.INVALID_END_DATE);
		}
		if (endDate.isAfter(startDate.plusMonths(3))) {
			throw new CustomException(ErrorCode.INVALID_END_DATE);
		}
	}

}
