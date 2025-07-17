package com.salayo.locallifebackend.domain.program.service;


import com.salayo.locallifebackend.domain.category.entity.AptitudeCategory;
import com.salayo.locallifebackend.domain.category.entity.RegionCategory;
import com.salayo.locallifebackend.domain.category.repository.AptitudeCategoryRepository;
import com.salayo.locallifebackend.domain.category.repository.RegionCategoryRepository;
import com.salayo.locallifebackend.domain.file.repository.FileRepository;
import com.salayo.locallifebackend.domain.localcreator.entity.LocalCreator;
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
import com.salayo.locallifebackend.domain.program.repository.ProgramDayRepository;
import com.salayo.locallifebackend.domain.program.repository.ProgramRepository;
import com.salayo.locallifebackend.global.enums.DeletedStatus;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
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
	 * - TODO : 하드 코드 값 수정(userId)
	 * - file 로직 추가
	 */
	public ProgramCreateResponseDto createProgram(long userId, @Valid ProgramCreateRequestDto requestDto) {

		Member member = memberRepository.findByIdOrElseThrow(userId);

		LocalCreator localCreator = localCreatorRepository.findByUserIdAndCreatorStatus(member.getId(), "APPROVED");
		String businessName = localCreator.getBusinessName();

		Long aptitudeCategoryId = requestDto.getAptitudeCategoryId();
		AptitudeCategory aptitudeCategory = aptitudeCategoryRepository.findByIdOrElseThrow(aptitudeCategoryId);

		Long regionCategoryId = requestDto.getRegionCategoryId();
		RegionCategory regionCategory = regionCategoryRepository.findByIdOrElseThrow(regionCategoryId);

		BigDecimal price = requestDto.getPrice();
		BigDecimal percent = requestDto.getPercent();
		BigDecimal discountedPrice = null;

		if (percent != null) {
			BigDecimal discountRate = BigDecimal.ONE.subtract(percent.divide(BigDecimal.valueOf(100)));
			discountedPrice = price.multiply(discountRate);
		}

		//체험 프로그램 객체 생성
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
			.minCapacity(requestDto.getMinCapacity())
			.maxCapacity(requestDto.getMaxCapacity())
			.startDate(requestDto.getStartDate())
			.endDate(requestDto.getEndDate())
			.count(0)
			.isLocalSpecialized(LocalSpecialized.GENERAL)
			.programStatus(ProgramStatus.REGISTERED)
			.deletedStatus(DeletedStatus.DISPLAYED)
			.build();

		programRepository.save(program);

		//요일 매핑
		List<ProgramDay> programDays = requestDto.getProgramDays().stream()
			.map(dayName -> {
				ProgramDay programDay = ProgramDay.builder()
					.program(program)
					.dayName(dayName)
					.build();
				program.getProgramDays().add(programDay);
				return programDay;
			})
			.toList();

		//스케줄 시간 매핑 및 저장
		List<ProgramScheduleTime> programScheduleTimes = requestDto.getProgramScheduleTimes().stream()
			.map(scheduleDto -> {
				Integer scheduleCount = scheduleDto.getScheduleCount();
				Integer scheduleDurationHours = scheduleDto.getScheduleDuration();
				LocalTime startTime = scheduleDto.getStartTime();
				LocalTime endTime = startTime.plusHours(scheduleDurationHours);

				ProgramScheduleTime scheduleTime = ProgramScheduleTime.builder()
					.program(program)
					.scheduleCount(scheduleCount)
					.scheduleDuration(scheduleDurationHours)
					.startTime(startTime)
					.endTime(endTime)
					.build();

				program.getProgramScheduleTimes().add(scheduleTime);
				return scheduleTime;
			})
			.toList();

		return ProgramCreateResponseDto.builder().build();
	}

}
