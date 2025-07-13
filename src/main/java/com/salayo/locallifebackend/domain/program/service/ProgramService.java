package com.salayo.locallifebackend.domain.program.service;


import com.salayo.locallifebackend.domain.category.repository.AptitudeCategoryRepository;
import com.salayo.locallifebackend.domain.category.repository.RegionCategoryRepository;
import com.salayo.locallifebackend.domain.file.repository.FileRepository;
import com.salayo.locallifebackend.domain.localcreator.repository.LocalCreatorRepository;
import com.salayo.locallifebackend.domain.member.repository.MemberRepository;
import com.salayo.locallifebackend.domain.program.repository.ProgramRepository;
import org.springframework.stereotype.Service;


@Service
public class ProgramService {

	private final AptitudeCategoryRepository aptitudeCategoryRepository;
	private final RegionCategoryRepository regionCategoryRepository;
	private final ProgramRepository programRepository;
	private final MemberRepository memberRepository;
	private final LocalCreatorRepository localCreatorRepository;
	private final FileRepository fileRepository;

	public ProgramService(AptitudeCategoryRepository aptitudeCategoryRepository, RegionCategoryRepository regionCategoryRepository,
		ProgramRepository programRepository, MemberRepository memberRepository, LocalCreatorRepository localCreatorRepository,
		FileRepository fileRepository) {
		this.aptitudeCategoryRepository = aptitudeCategoryRepository;
		this.regionCategoryRepository = regionCategoryRepository;
		this.programRepository = programRepository;
		this.memberRepository = memberRepository;
		this.localCreatorRepository = localCreatorRepository;
		this.fileRepository = fileRepository;
	}

	/**
	 * 체험 프로그램 생성 메서드
	 * - TODO : 구현 예정
	 */

}
