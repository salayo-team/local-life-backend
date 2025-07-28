package com.salayo.locallifebackend.global.dto;


import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Builder
@Getter
public class PaginationResponseDto<T> {

	private List<T> content;

	private int page;

	private int size;

	private long totalElements;

	private int totalPages;

	public PaginationResponseDto(List<T> content, int page, int size, long totalElements, int totalPages) {
		this.content = content;
		this.page = page;
		this.size = size;
		this.totalElements = totalElements;
		this.totalPages = totalPages;
	}

	public static <T> PaginationResponseDto<T> of(Page<T> page){
		return PaginationResponseDto.<T>builder()
			.content(page.getContent())
			.page(page.getNumber())
			.size(page.getSize())
			.totalElements(page.getTotalElements())
			.totalPages(page.getTotalPages())
			.build();
	}

}
