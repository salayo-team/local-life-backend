package com.salayo.locallifebackend.domain.program.entity;

import com.salayo.locallifebackend.domain.category.entity.AptitudeCategory;
import com.salayo.locallifebackend.domain.category.entity.RegionCategory;
import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.program.enums.LocalSpecialized;
import com.salayo.locallifebackend.domain.program.enums.ProgramStatus;
import com.salayo.locallifebackend.global.entity.BaseEntity;
import com.salayo.locallifebackend.global.enums.DeletedStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "experience_program")
public class Program extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; //체험 프로그램 고유 식별자

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "provider_id", nullable = false)
	private Member member; //프로그램 제공자(로컬 크리에이터) 고유 식별자

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "aptitude_category_id", nullable = false)
	private AptitudeCategory aptitudeCategory; //적성 카테고리 고유 식별자

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "region_category_id", nullable = false)
	private RegionCategory regionCategory; //지역 카테고리 고유 식별자

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "original_program_id", nullable = true)
	private Program originalProgram; //원본 프로그램 ID - 자기참조 FK

	@Column(nullable = false, length = 100)
	private String businessName; //상호명

	@Column(nullable = false, length = 100)
	private String title; //프로그램 제목

	@Column(nullable = false, columnDefinition = "TEXT")
	private String description; //프로그램 설명

	@Column(nullable = false, columnDefinition = "TEXT")
	private String curriculumDescription; //프로그램 커리큘럼 설명

	@Column(nullable = false, length = 255)
	private String location; //체험 위치

	@Column(nullable = false)
	private BigDecimal price; //체험 가격

	@Column(nullable = true)
	private BigDecimal percent; //체험 할인율

	@Column(nullable = true)
	private BigDecimal discountedPrice; //할인된 가격

	@Column(nullable = false)
	private Integer maxCapacity; //스케줄 체험 정원

	@Column(nullable = false)
	private Integer minCapacity; //스케줄 최소 정원

	@Column(nullable = false)
	private LocalDate startDate; //체험 프로그램 시작일

	@Column(nullable = false)
	private LocalDate endDate; //체험 프로그램 종료일

	@Column(nullable = false)
	private Integer count; //조회수

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private LocalSpecialized isLocalSpecialized; //지역특화 여부

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private ProgramStatus programStatus; //프로그램 운영 상태

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private DeletedStatus deletedStatus; //프로그램 삭제 여부

	@OneToMany(mappedBy = "program", cascade = CascadeType.ALL, orphanRemoval = false)
	private List<ProgramDay> programDays = new ArrayList<>(); //요일

	@OneToMany(mappedBy = "program", cascade = CascadeType.ALL, orphanRemoval = false)
	private List<ProgramScheduleTime> programScheduleTimes = new ArrayList<>();

	@Builder
	public Program(Member member, AptitudeCategory aptitudeCategory, RegionCategory regionCategory, Program originalProgram,
		String businessName, String title, String description, String curriculumDescription, String location, BigDecimal price,
		BigDecimal percent, BigDecimal discountedPrice, Integer maxCapacity, Integer minCapacity, LocalDate startDate,
		LocalDate endDate, Integer count, LocalSpecialized isLocalSpecialized, ProgramStatus programStatus, DeletedStatus deletedStatus,
		List<ProgramDay> programDays, List<ProgramScheduleTime> programScheduleTimes) {
		this.member = member;
		this.aptitudeCategory = aptitudeCategory;
		this.regionCategory = regionCategory;
		this.originalProgram = originalProgram;
		this.businessName = businessName;
		this.title = title;
		this.description = description;
		this.curriculumDescription = curriculumDescription;
		this.location = location;
		this.price = price;
		this.percent = percent;
		this.discountedPrice = discountedPrice;
		this.maxCapacity = maxCapacity;
		this.minCapacity = minCapacity;
		this.startDate = startDate;
		this.endDate = endDate;
		this.count = count;
		this.isLocalSpecialized = isLocalSpecialized;
		this.programStatus = programStatus;
		this.deletedStatus = deletedStatus;
		this.programDays = programDays != null ? programDays : new ArrayList<ProgramDay>();
		this.programScheduleTimes = programScheduleTimes != null ? programScheduleTimes : new ArrayList<ProgramScheduleTime>();
	}

}
