package com.salayo.locallifebackend.domain.category.entity;

import com.salayo.locallifebackend.domain.program.entity.Program;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "experience_program")
public class AptitudeCategory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; //적성 카테고리 고유 식별자

	@OneToOne(mappedBy = "aptitudeCategory")
	private Program program;

}
