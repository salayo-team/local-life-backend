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
import com.salayo.locallifebackend.domain.program.dto.ProgramDetailResponseDto;
import com.salayo.locallifebackend.domain.program.dto.ProgramListResponseDto;
import com.salayo.locallifebackend.domain.program.dto.ProgramSearchRequestDto;
import com.salayo.locallifebackend.domain.program.entity.Program;
import com.salayo.locallifebackend.domain.program.entity.ProgramDay;
import com.salayo.locallifebackend.domain.program.entity.ProgramScheduleTime;
import com.salayo.locallifebackend.domain.program.enums.DayName;
import com.salayo.locallifebackend.domain.program.enums.ProgramStatus;
import com.salayo.locallifebackend.domain.program.repository.ProgramRepository;
import com.salayo.locallifebackend.domain.programschedule.entity.ProgramSchedule;
import com.salayo.locallifebackend.domain.programschedule.enums.ProgramScheduleStatus;
import com.salayo.locallifebackend.domain.programschedule.repository.ProgramScheduleRepository;
import com.salayo.locallifebackend.domain.reservation.enums.ReservationStatus;
import com.salayo.locallifebackend.domain.reservation.repository.ReservationRepository;
import com.salayo.locallifebackend.global.dto.PaginationResponseDto;
import com.salayo.locallifebackend.global.enums.DeletedStatus;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
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
	private final ReservationRepository reservationRepository;
	private final ProgramScheduleRepository programScheduleRepository;

	public ProgramService(AptitudeCategoryRepository aptitudeCategoryRepository, RegionCategoryRepository regionCategoryRepository,
		ProgramRepository programRepository, MemberRepository memberRepository, LocalCreatorRepository localCreatorRepository,
		FileRepository fileRepository, S3Uploader s3Uploader, FileMappingRepository fileMappingRepository,
		ReservationRepository reservationRepository, ProgramScheduleRepository programScheduleRepository) {
		this.aptitudeCategoryRepository = aptitudeCategoryRepository;
		this.regionCategoryRepository = regionCategoryRepository;
		this.programRepository = programRepository;
		this.memberRepository = memberRepository;
		this.localCreatorRepository = localCreatorRepository;
		this.fileRepository = fileRepository;
		this.s3Uploader = s3Uploader;
		this.fileMappingRepository = fileMappingRepository;
		this.reservationRepository = reservationRepository;
		this.programScheduleRepository = programScheduleRepository;
	}

	/**
	 * 체험 프로그램 생성 메서드
	 */
	@Transactional
	public ProgramCreateResponseDto createProgram(long memberId, @Valid ProgramCreateRequestDto requestDto, List<MultipartFile> files,
		List<FilePurpose> filePurposes) {

		Member member = memberRepository.findActiveByIdOrThrow(memberId);

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
		BigDecimal finalPrice = price;
		if (percent != null && percent.compareTo(BigDecimal.ZERO) > 0) {
			BigDecimal discountAmount = price.multiply(percent)
				.divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);

			finalPrice = price.subtract(discountAmount);
			if (finalPrice.compareTo(BigDecimal.ONE) < 0) {
				throw new CustomException(ErrorCode.INVALID_FINAL_PRICE);
			}
		}

		Integer minCapacity = requestDto.getMinCapacity();
		Integer maxCapacity = requestDto.getMaxCapacity();
		if (minCapacity > maxCapacity) {
			throw new CustomException(ErrorCode.INVALID_CAPACITY_RANGE);
		}

		LocalDate startDate = requestDto.getStartDate();
		validateStartDate(startDate);

		LocalDate endDate = requestDto.getEndDate();
		validateEndDate(startDate, endDate);

		Program program = Program.createProgram(
			member,
			aptitudeCategory,
			regionCategory,
			businessName,
			requestDto.getTitle(),
			requestDto.getDescription(),
			requestDto.getCurriculumDescription(),
			requestDto.getLocation(),
			price,
			percent,
			finalPrice,
			minCapacity,
			maxCapacity,
			startDate,
			endDate
		);

		requestDto.getProgramDays().forEach(dayName -> {
			ProgramDay programDay = ProgramDay.createProgramDay(dayName);
			program.addProgramDay(programDay);
		});

		requestDto.getProgramScheduleTimes().forEach(scheduleDto -> {
			Integer scheduleDurationHours = scheduleDto.getScheduleDuration();
			LocalTime startTime = scheduleDto.getStartTime();

			LocalTime endTime = startTime.plusHours(scheduleDurationHours);
			if (endTime.isBefore(startTime)) {
				throw new CustomException(ErrorCode.SCHEDULE_TIME_CANNOT_CROSS_DAY);
			}

			ProgramScheduleTime scheduleTime = ProgramScheduleTime.createProgramScheduleTime(
				scheduleDto.getScheduleCount(),
				scheduleDurationHours,
				startTime,
				endTime
			);
			program.addProgramScheduleTime(scheduleTime);
		});

		validateDuplicateScheduleTime(memberId, program);

		createProgramSchedulesForProgram(program);
		programRepository.save(program);

		saveProgramFiles(program, files, filePurposes);

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
	 * - 회차에 따라 하루 스케줄 개수 결정
	 */
	private void createProgramSchedulesForProgram(Program program) {

		LocalDate currentDate = program.getStartDate();
		LocalDate endDate = program.getEndDate();

		List<DayName> programDays = program.getProgramDays().stream()
			.map(ProgramDay::getDayName)
			.toList();

		while (!currentDate.isAfter(endDate)) {
			DayName currentDay = DayName.valueOf(currentDate.getDayOfWeek().name());

			if (programDays.contains(currentDay)) {

				for (ProgramScheduleTime programScheduleTime : program.getProgramScheduleTimes()) {
					ProgramSchedule programSchedule = ProgramSchedule.createProgramSchedule(
						program,
						currentDate,
						programScheduleTime.getStartTime(),
						programScheduleTime.getEndTime()
					);
					program.addProgramSchedule(programSchedule);
				}
			}

			currentDate = currentDate.plusDays(1);
		}
	}

	/**
	 * S3 업로드 & 파일 생성 메서드
	 */
	private void saveProgramFiles(Program program, List<MultipartFile> files, List<FilePurpose> filePurposes) {

		validateFileInputAndPurposes(files, filePurposes);

		for (int i = 0; i < files.size(); i++) {
			MultipartFile multipartFile = files.get(i);
			FilePurpose filePurpose = filePurposes.get(i);

			String storedFileUrl = s3Uploader.upload(multipartFile, "program");

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
	}

	/**
	 * 기존에 생성한 체험 프로그램 스케줄과 날짜 & 시간이 겹치는지 검증하는 메서드
	 */
	private void validateDuplicateScheduleTime(Long memberId, Program program) {

		LocalDate startDate = program.getStartDate();
		LocalDate endDate = program.getEndDate();

		Set<DayName> programDays = program.getProgramDays().stream()
			.map(ProgramDay::getDayName)
			.collect(Collectors.toSet());

		List<ProgramScheduleTime> newProgramScheduleTimes = program.getProgramScheduleTimes();

		Set<LocalDate> newScheduleDates = new HashSet<>();
		LocalDate currentDate = startDate;

		while (!currentDate.isAfter(endDate)) {

			DayName dayName = DayName.valueOf(currentDate.getDayOfWeek().name());
			if (programDays.contains(dayName)) {
				newScheduleDates.add(currentDate);
			}
			currentDate = currentDate.plusDays(1);
		}

		List<ProgramSchedule> existingSchedules = programScheduleRepository.findActiveSchedulesByMemberAndDateRange(
			memberId,
			DeletedStatus.DISPLAYED,
			ProgramScheduleStatus.ACTIVE,
			startDate,
			endDate
		);

		boolean hasConflictScheduleFound = false;

		for (ProgramSchedule existing : existingSchedules) {

			if (!newScheduleDates.contains(existing.getScheduleDate())) {
				continue;
			}

			LocalTime existingStartTime = existing.getStartTime();
			LocalTime existingEndTime = existing.getEndTime();

			for (ProgramScheduleTime newProgramScheduleTime : newProgramScheduleTimes) {

				LocalTime newStartTime = newProgramScheduleTime.getStartTime();
				boolean startTimeWithinBlockRange =
					!newStartTime.isBefore(existingStartTime) && !newStartTime.isAfter(existingEndTime);

				if (startTimeWithinBlockRange) {
					hasConflictScheduleFound = true;
					break;
				}
			}

			if (hasConflictScheduleFound) {
				break;
			}
		}

		if (hasConflictScheduleFound) {
			throw new CustomException(ErrorCode.DUPLICATE_PROGRAM_SCHEDULE_TIME_FOR_MEMBER);
		}
	}

	/**
	 * 체험 프로그램 검색 메서드
	 * - 멤버가 체험 프로그램 정렬 조건을 설정하여 검색
	 */
	@Transactional(readOnly = true)
	public PaginationResponseDto<ProgramListResponseDto> searchProgram(ProgramSearchRequestDto requestDto) {

		BigDecimal minPrice = requestDto.getMinPrice();
		BigDecimal maxPrice = requestDto.getMaxPrice();

		if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
			throw new CustomException(ErrorCode.INVALID_PRICE_RANGE);
		}

		String normalizedKeyword = normalizationKeyword(requestDto.getKeyword());
		if (normalizedKeyword == null) {
			throw new CustomException(ErrorCode.INVALID_SEARCH_KEYWORD);
		}
		requestDto.setKeyword(normalizedKeyword);

		Page<Program> programPage = programRepository.searchPrograms(requestDto);

		Page<ProgramListResponseDto> programContent = programPage
			.map(program -> {
				String thumbnailUrl = getProgramThumbnailUrl(program.getId());
				return ProgramListResponseDto.from(program, thumbnailUrl);
			});

		return PaginationResponseDto.of(programContent);
	}

	/**
	 * 이모지, 특수문자, 공백 정리 메서드
	 */
	private String normalizationKeyword(String keyword) {

		if (keyword == null) {
			return null;
		}

		String removeEmoji = keyword.replaceAll("[\\p{So}\\p{Cn}]", " ");
		String removeSpecialCharacters = removeEmoji.replaceAll("[^가-힣a-zA-Z0-9\\s]", " ");
		String normalized = removeSpecialCharacters.trim().replaceAll("\\s{2,}", " ");

		if (normalized.isEmpty()) {
			return null;
		}

		return normalized;
	}

	/**
	 * 체험 프로그램 삭제 메서드
	 */
	@Transactional
	public void deleteProgram(Long programId, Long memberId) {

		Program program = programRepository.findByIdOrElseThrow(programId);
		Long programOwner = program.getMember().getId();
		if (!programOwner.equals(memberId)) {
			throw new CustomException(ErrorCode.PROGRAM_NOT_FOUND_FOR_USER);
		}

		LocalDate today = LocalDate.now();
		if (program.getStartDate().isBefore(today.plusDays(7))) {
			throw new CustomException(ErrorCode.CANNOT_DELETE_BEFORE_START);
		}

		List<Long> scheduleIds = program.getProgramSchedules().stream()
			.map(ProgramSchedule::getId)
			.toList();

		Set<ReservationStatus> inactiveReservationStatuses = ReservationStatus.inactiveReservationStatusSet();
		boolean existsActiveReservation =
			reservationRepository.existsByProgramSchedule_IdInAndReservationStatusNotIn(
				scheduleIds,
				inactiveReservationStatuses
			);
		if (existsActiveReservation) {
			throw new CustomException(ErrorCode.CANNOT_DELETE_ACTIVE_RESERVATION_EXIST);
		}

		program.getProgramSchedules().forEach(ProgramSchedule::deleteProgramSchedule);
		program.softDelete();
	}

	/**
	 * 로컬 크리에이터 체험 프로그램 전체 리스트 조회 메서드
	 * - 로컬 크리에이터가 생성한 체험 프로그램 전체 리스트 조회
	 */
	@Transactional(readOnly = true)
	public PaginationResponseDto<ProgramListResponseDto> getProgramForLocalCreator(Long memberId, int page, int size) {

		Member member = memberRepository.findActiveByIdOrThrow(memberId);

		Pageable pageable = PageRequest.of(page, size, Sort.by(Direction.DESC, "id"));
		Page<Program> programPage = programRepository.findByMemberIdAndDeletedStatus(
			member.getId(),
			DeletedStatus.DISPLAYED,
			pageable
		);
		Page<ProgramListResponseDto> programContent = programPage
			.map(program -> {
				String thumbnailUrl = getProgramThumbnailUrl(program.getId());
				return ProgramListResponseDto.from(program, thumbnailUrl);
			});

		return PaginationResponseDto.of(programContent);
	}

	/**
	 * 체험 프로그램 단건 상세 조회 메서드
	 * - 멤버가 체험 프로그램 상세 조회
	 */
	@Transactional(readOnly = true)
	public ProgramDetailResponseDto getProgramDetail(Long programId) {

		Program program = programRepository.findByIdOrElseThrow(programId);
		if (program.getDeletedStatus() == DeletedStatus.DELETED) {
			throw new CustomException(ErrorCode.PROGRAM_NOT_FOUND);
		}
		if (program.getProgramStatus() != ProgramStatus.REGISTERED) {
			throw new CustomException(ErrorCode.PROGRAM_STATUS_NOT_VIEWABLE);
		}

		String thumbnailUrl = getProgramThumbnailUrl(program.getId());
		List<String> detailImageUrls = getProgramDetailImageUrls(program.getId());

		return ProgramDetailResponseDto.from(program, thumbnailUrl, detailImageUrls);
	}

	/**
	 * 프로그램 썸네일 url 조회 메서드
	 */
	private String getProgramThumbnailUrl(Long programId) {

		String thumbnailUrl = fileMappingRepository.findAllByFileCategoryAndReferenceIdAndFilePurpose(
				FileCategory.PROGRAM,
				programId,
				FilePurpose.THUMBNAIL
			).stream()
			.findFirst()
			.map(mapping -> mapping.getFile().getStoredFileName())
			.orElse(null);

		return thumbnailUrl;
	}

	/**
	 * 프로그램 이미지 url 리스트 조회 메서드
	 */
	private List<String> getProgramDetailImageUrls(Long programId) {

		List<FileMapping> fileMappings = fileMappingRepository.findAllByFileCategoryAndReferenceIdAndFilePurpose(
			FileCategory.PROGRAM,
			programId,
			FilePurpose.DETAIL_IMAGE
		);

		return fileMappings.stream()
			.map(mapping -> mapping.getFile().getStoredFileName())
			.collect(Collectors.toList());
	}

}
