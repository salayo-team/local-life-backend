package com.salayo.locallifebackend.domain.program.entity;

import com.salayo.locallifebackend.domain.program.enums.DayName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "program_day")
public class ProgramDay {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; //체험 프로그램 요일 고유 식별자

	@Enumerated(EnumType.STRING)
	@Column(name = "day_name", nullable = false, length = 50)
	private DayName dayName; //요일명

	@Builder
	public ProgramDay(DayName dayName) {
		this.dayName = dayName;

	}

}
