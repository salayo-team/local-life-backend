package com.salayo.locallifebackend.domain.program.dto;

import com.salayo.locallifebackend.domain.program.enums.SortType;
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

	private List<Long> aptitudeIds;

	private List<Long> regionIds;

	private SortType sort;

	private Integer page = DEFAULT_PAGE;

	private Integer size = DEFAULT_SIZE;

}
