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
@Table(name = "region_category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegionCategory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; //지역 카테고리 고유 식별자

	private String regionName; //지역 이름

	public RegionCategory(String regionName) {
		this.regionName = regionName;
	}
}
