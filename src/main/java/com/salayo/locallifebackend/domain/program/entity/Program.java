package com.salayo.locallifebackend.domain.program.entity;

import com.salayo.locallifebackend.domain.category.entity.AptitudeCategory;
import com.salayo.locallifebackend.domain.category.entity.RegionCategory;
import com.salayo.locallifebackend.domain.program.enums.ProgramStatus;
import com.salayo.locallifebackend.global.entity.BaseEntity;
import com.salayo.locallifebackend.global.enums.DeletedStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
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

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "aptitude_category_id")
	private AptitudeCategory aptitudeCategory; //적성 카테고리 고유 식별자

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "region_category_id")
	private RegionCategory regionCategory; //지역 카테고리 고유 식별자

	@Column(nullable = false, length = 100)
	private String businessName; //상호명

	@Column(nullable = false, length = 100)
	private String title; //프로그램 제목

	@Column(nullable = false, columnDefinition = "TEXT")
	private String description; //프로그램 설명

	@Column(nullable = false, length = 255)
	private String location; //체험 위치

	@Column(nullable = false)
	private BigDecimal price; //체험 가격

	@Column(nullable = true)
	private BigDecimal percent; //체험 할인율

	@Column(nullable = false)
	private Integer capacity; //체험 정원

	@Column(nullable = false)
	private LocalDate recruitmentPeriod; //모집 기간

	@Column(nullable = false)
	private LocalDate startDate; //체험 시작일

	@Column(nullable = false)
	private LocalDate endDate; //체험 종료일

	@Column(nullable = false)
	private LocalTime startTime; //체험 시작시간

	@Column(nullable = false)
	private LocalTime endTime; //체험 종료시간

	@Column(nullable = false)
	private Integer count; //조회수

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ProgramStatus programStatus; //프로그램 운영 상태

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private DeletedStatus deletedStatus; //프로그램 삭제 여부

	@Builder
	public Program(AptitudeCategory aptitudeCategory, RegionCategory regionCategory, String businessName, String title, String description,
		String location, BigDecimal price, BigDecimal percent, Integer capacity, LocalDate recruitmentPeriod, LocalDate startDate,
		LocalDate endDate, LocalTime startTime, LocalTime endTime) {
		this.aptitudeCategory = aptitudeCategory;
		this.regionCategory = regionCategory;
		this.businessName = businessName;
		this.title = title;
		this.description = description;
		this.location = location;
		this.price = price;
		this.percent = percent;
		this.capacity = capacity;
		this.recruitmentPeriod = recruitmentPeriod;
		this.startDate = startDate;
		this.endDate = endDate;
		this.startTime = startTime;
		this.endTime = endTime;
		this.count = count;
		this.programStatus = programStatus;
		this.deletedStatus = deletedStatus;
	}

}
