package com.salayo.locallifebackend.domain.program.dto;

import com.salayo.locallifebackend.domain.program.enums.LocalSpecialized;
import com.salayo.locallifebackend.domain.program.enums.SortType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ProgramSearchRequestDto {

	public static final int DEFAULT_PAGE = 0;
	public static final int DEFAULT_SIZE = 10;
	public static final int MAX_SIZE = 100;

	private String keyword;

	private LocalSpecialized isLocalSpecialized;

	@DecimalMin(value = "1.0", message = "체험 프로그램 최소 금액은 {value}원 이상이여야 합니다.")
	@DecimalMax(value = "5000000.0", message = "체험 프로그램 최소 금액은 {value}원 이하여야 합니다.")
	@Digits(integer = 7, fraction = 0, message = "체험 금액은 정수만 입력 가능합니다.")
	private BigDecimal minPrice;

	@DecimalMin(value = "1.0", message = "체험 프로그램 최대 금액은 {value}원 이상이여야 합니다.")
	@DecimalMax(value = "5000000.0", message = "체험 프로그램 최대 금액은 {value}원 이하여야 합니다.")
	@Digits(integer = 7, fraction = 0, message = "체험 금액은 정수만 입력 가능합니다.")
	private BigDecimal maxPrice;

	private List<Long> aptitudeIds;

	private List<Long> regionIds;

	private SortType sort;

	@Min(value = 0, message = "페이지 번호는 0 이상어야 합니다.")
	private Integer page = DEFAULT_PAGE;

	@Min(value = 1, message = "페이지당 항목 수는 1 이상이어야 합니다.")
	@Max(value = MAX_SIZE, message = "페이지당 항목 수는 최대 " + MAX_SIZE + "개까지 가능합니다.")
	private Integer size = DEFAULT_SIZE;

}
