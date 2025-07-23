package com.salayo.locallifebackend.domain.magazine.service;

import com.salayo.locallifebackend.domain.category.entity.AptitudeCategory;
import com.salayo.locallifebackend.domain.category.entity.RegionCategory;
import com.salayo.locallifebackend.domain.category.repository.AptitudeCategoryRepository;
import com.salayo.locallifebackend.domain.category.repository.RegionCategoryRepository;
import com.salayo.locallifebackend.domain.magazine.dto.MagazineCreateRequestDto;
import com.salayo.locallifebackend.domain.magazine.entity.Magazine;
import com.salayo.locallifebackend.domain.magazine.enums.MagazineStatus;
import com.salayo.locallifebackend.domain.magazine.repository.MagazineRepository;
import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.member.repository.MemberRepository;
import com.salayo.locallifebackend.global.enums.DeletedStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MagazineService {

    private final MagazineRepository magazineRepository;
    private final RegionCategoryRepository regionCategoryRepository;
    private final AptitudeCategoryRepository aptitudeCategoryRepository;
    private final MemberRepository memberRepository;

    public MagazineService(MagazineRepository magazineRepository, RegionCategoryRepository regionCategoryRepository,
        AptitudeCategoryRepository aptitudeCategoryRepository, MemberRepository memberRepository) {
        this.magazineRepository = magazineRepository;
        this.regionCategoryRepository = regionCategoryRepository;
        this.aptitudeCategoryRepository = aptitudeCategoryRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public void createMagazine(MagazineCreateRequestDto createRequestDto, Long adminId) {
        RegionCategory region = regionCategoryRepository.findByIdOrElseThrow(createRequestDto.getRegionCategoryId());
        AptitudeCategory aptitude = aptitudeCategoryRepository.findByIdOrElseThrow(createRequestDto.getAptitudeCategoryId());
        Member admin = memberRepository.findByIdOrElseThrow(adminId);

        Magazine magazine = Magazine.builder()
            .title(createRequestDto.getTitle())
            .content(createRequestDto.getContent())
            .thumbnailUrl(createRequestDto.getThumbnailUrl())
            .magazineStatus(MagazineStatus.DRAFT)
            .deletedStatus(DeletedStatus.DISPLAYED)
            .views(0L)
            .regionCategory(region)
            .aptitudeCategory(aptitude)
            .admin(admin)
            .build();

        magazineRepository.save(magazine);
    }

}
