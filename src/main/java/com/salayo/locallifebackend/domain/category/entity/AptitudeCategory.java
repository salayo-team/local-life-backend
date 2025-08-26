package com.salayo.locallifebackend.domain.category.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "aptitude_category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AptitudeCategory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; //적성 카테고리 고유 식별자

	private String aptitudeName; //적성 이름

	private String aptitudeCode; //적성 코드

	public AptitudeCategory(String aptitudeName, String aptitudeCode) {
		this.aptitudeName = aptitudeName;
		this.aptitudeCode = aptitudeCode;
	}

}
