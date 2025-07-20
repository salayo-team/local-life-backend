package com.salayo.locallifebackend.domain.program.service;


import com.salayo.locallifebackend.domain.category.entity.AptitudeCategory;
import com.salayo.locallifebackend.domain.category.entity.RegionCategory;
import com.salayo.locallifebackend.domain.category.repository.AptitudeCategoryRepository;
import com.salayo.locallifebackend.domain.category.repository.RegionCategoryRepository;
import com.salayo.locallifebackend.domain.file.entity.File;
import com.salayo.locallifebackend.domain.file.entity.FileMapping;
import com.salayo.locallifebackend.domain.file.enums.FileCategory;
import com.salayo.locallifebackend.domain.file.enums.FilePurpose;
import com.salayo.locallifebackend.domain.file.repository.FileMappingRepository;
import com.salayo.locallifebackend.domain.file.repository.FileRepository;
import com.salayo.locallifebackend.domain.file.util.S3Uploader;
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
import com.salayo.locallifebackend.domain.program.enums.DayName;
import com.salayo.locallifebackend.domain.program.enums.LocalSpecialized;
import com.salayo.locallifebackend.domain.program.enums.ProgramStatus;
import com.salayo.locallifebackend.domain.program.repository.ProgramRepository;
import com.salayo.locallifebackend.domain.programschedule.entity.ProgramSchedule;
import com.salayo.locallifebackend.domain.programschedule.enums.ProgramScheduleStatus;
import com.salayo.locallifebackend.global.enums.DeletedStatus;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


@Service
public class ProgramService {

	private final AptitudeCategoryRepository aptitudeCategoryRepository;
	private final RegionCategoryRepository regionCategoryRepository;
	private final ProgramRepository programRepository;
	private final MemberRepository memberRepository;
	private final LocalCreatorRepository localCreatorRepository;
	private final FileRepository fileRepository;
	private final S3Uploader s3Uploader;
	private final FileMappingRepository fileMappingRepository;

	public ProgramService(AptitudeCategoryRepository aptitudeCategoryRepository, RegionCategoryRepository regionCategoryRepository,
		ProgramRepository programRepository, MemberRepository memberRepository, LocalCreatorRepository localCreatorRepository,
		FileRepository fileRepository, S3Uploader s3Uploader, FileMappingRepository fileMappingRepository) {
		this.aptitudeCategoryRepository = aptitudeCategoryRepository;
		this.regionCategoryRepository = regionCategoryRepository;
		this.programRepository = programRepository;
		this.memberRepository = memberRepository;
		this.localCreatorRepository = localCreatorRepository;
		this.fileRepository = fileRepository;
		this.s3Uploader = s3Uploader;
		this.fileMappingRepository = fileMappingRepository;
	}

	/**
	 * 체험 프로그램 생성 메서드
	 * - TODO : 동일한 유저가 중복되는 스케줄 타임 생성시 예외처리
	 * - TODO : 스케줄 종료시간 - 마지막 스케줄 시간 설정시, 소요시간 더해서 다음 날짜로 넘어가면 안되도록 예외처리
	 */
	@Transactional
	public ProgramCreateResponseDto createProgram(long memberId, @Valid ProgramCreateRequestDto requestDto, List<MultipartFile> files,
		List<FilePurpose> filePurposes) {

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

		BigDecimal finalPrice = price;

		if (percent != null) {
			BigDecimal discountRate = BigDecimal.ONE.subtract(percent.divide(BigDecimal.valueOf(100)));
			finalPrice = price.multiply(discountRate);
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
			.finalPrice(finalPrice)
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

		createProgramSchedule(program);
		programRepository.save(program);

		validateFileInputAndPurposes(files, filePurposes);

		for (int i = 0; i < files.size(); i++) {
			MultipartFile multipartFile = files.get(i);
			FilePurpose filePurpose = filePurposes.get(i);

			String storedFileUrl = s3Uploader.upload(multipartFile, "programs");

			File fileEntity = File.builder()
				.originalName(multipartFile.getOriginalFilename())
				.storedFileName(storedFileUrl)
				.build();

			fileRepository.save(fileEntity);

			FileMapping fileMapping = FileMapping.builder()
				.file(fileEntity)
				.fileCategory(FileCategory.PROGRAM)
				.referenceId(program.getId())
				.filePurpose(filePurpose)
				.build();

			fileMappingRepository.save(fileMapping);
		}

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

	/**
	 * 파일 검증 메서드
	 * - 파일 타입, 파일 개수, 파일 목적, 썸네일 개수 검증
	 */
	private void validateFileInputAndPurposes(List<MultipartFile> files, List<FilePurpose> filePurposes) {
		if (files == null || files.isEmpty() || filePurposes == null || files.size() != filePurposes.size()) {
			throw new CustomException(ErrorCode.INVALID_FILE_PURPOSE_MAPPING);
		}

		if (files.size() > 10) {
			throw new CustomException(ErrorCode.FILE_COUNT_EXCEEDED);
		}

		int thumbnailCount = 0;

		for (int i = 0; i < files.size(); i++) {
			MultipartFile multipartFile = files.get(i);
			FilePurpose filePurpose = filePurposes.get(i);

			String fileType = multipartFile.getContentType();
			if (!List.of("image/jpg", "image/jpeg", "image/png").contains(fileType)) {
				throw new CustomException(ErrorCode.INVALID_FILE_TYPE);
			}

			if (filePurpose == FilePurpose.THUMBNAIL) {
				thumbnailCount++;

				if (thumbnailCount > 1) {
					throw new CustomException(ErrorCode.THUMBNAIL_LIMIT_EXCEEDED);
				}
			}
		}

		if (thumbnailCount == 0) {
			throw new CustomException(ErrorCode.THUMBNAIL_REQUIRED);
		}
	}

	/**
	 * 스케줄 생성 로직
	 * - 프로그램 시작일 ~ 종료일까지 사용자가 선택한 요일에 해당하는 날짜에 스케줄 생성
	 */
	private void createProgramSchedule(Program program) {
		LocalDate currentDate = program.getStartDate();
		LocalDate endDate = program.getEndDate();

		List<DayName> programDays = program.getProgramDays().stream()
			.map(ProgramDay::getDayName)
			.toList();

		while (!currentDate.isAfter(endDate)) {
			DayName currentDay = DayName.valueOf(currentDate.getDayOfWeek().name());

			if (programDays.contains(currentDay)) {

				for (ProgramScheduleTime programScheduleTime : program.getProgramScheduleTimes()) {
					ProgramSchedule programSchedule = ProgramSchedule.builder()
						.program(program)
						.scheduleDate(currentDate)
						.startTime(programScheduleTime.getStartTime())
						.endTime(programScheduleTime.getEndTime())
						.programScheduleStatus(ProgramScheduleStatus.ACTIVE)
						.deletedStatus(DeletedStatus.DISPLAYED)
						.build();

					program.addProgramSchedule(programSchedule);
				}
			}

			currentDate = currentDate.plusDays(1);
		}
	}

}
