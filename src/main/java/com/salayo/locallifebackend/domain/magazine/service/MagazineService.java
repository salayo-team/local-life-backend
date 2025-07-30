package com.salayo.locallifebackend.domain.magazine.service;

import com.salayo.locallifebackend.domain.category.entity.AptitudeCategory;
import com.salayo.locallifebackend.domain.category.entity.RegionCategory;
import com.salayo.locallifebackend.domain.category.repository.AptitudeCategoryRepository;
import com.salayo.locallifebackend.domain.category.repository.RegionCategoryRepository;
import com.salayo.locallifebackend.domain.magazine.dto.MagazineCreateRequestDto;
import com.salayo.locallifebackend.domain.magazine.dto.MagazineDraftDetailResponseDto;
import com.salayo.locallifebackend.domain.magazine.dto.MagazineDraftListResponseDto;
import com.salayo.locallifebackend.domain.magazine.entity.Magazine;
import com.salayo.locallifebackend.domain.magazine.enums.MagazineStatus;
import com.salayo.locallifebackend.domain.magazine.repository.MagazineRepository;
import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.member.repository.MemberRepository;
import com.salayo.locallifebackend.global.enums.DeletedStatus;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MagazineService {

    private final MagazineRepository magazineRepository;
    private final RegionCategoryRepository regionCategoryRepository;
    private final AptitudeCategoryRepository aptitudeCategoryRepository;
    private final MemberRepository memberRepository;
    private final MagazineFileService magazineFileService;

    public MagazineService(MagazineRepository magazineRepository, RegionCategoryRepository regionCategoryRepository,
        AptitudeCategoryRepository aptitudeCategoryRepository, MemberRepository memberRepository, MagazineFileService magazineFileService) {
        this.magazineRepository = magazineRepository;
        this.regionCategoryRepository = regionCategoryRepository;
        this.aptitudeCategoryRepository = aptitudeCategoryRepository;
        this.memberRepository = memberRepository;
        this.magazineFileService = magazineFileService;
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

        List<String> imageUrls = extractImageUrls(createRequestDto.getContent());
        for (String url : imageUrls) {
            magazineFileService.updateFileReferenceIdByUrl(url, magazine.getId());
        }
    }

    private List<String> extractImageUrls(String html) {
        Pattern pattern = Pattern.compile("<img\\s+[^>]*src=[\"']([^\"']+)[\"']");
        Matcher matcher = pattern.matcher(html);

        List<String> urls = new ArrayList<>();

        while (matcher.find()) {
            urls.add(matcher.group(1));
        }

        return urls;
    }

    public List<MagazineDraftListResponseDto> getDraftMagazines() {
        List<Magazine> magazines = magazineRepository.findByMagazineStatusAndDeletedStatus(MagazineStatus.DRAFT, DeletedStatus.DISPLAYED);

        return magazines.stream()
            .map(m -> MagazineDraftListResponseDto.builder()
                .id(m.getId())
                .title(m.getTitle())
                .thumbnailUrl(m.getThumbnailUrl())
                .regionName(m.getRegionCategory().getRegionName())
                .aptitudeName(m.getAptitudeCategory().getAptitudeName())
                .createdAt(m.getCreatedAt())
                .build())
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MagazineDraftDetailResponseDto getDraftMagazineDetail(Long magazineId) {
        Magazine magazine = magazineRepository.findById(magazineId)
            .orElseThrow(() -> new CustomException(ErrorCode.MAGAZINE_NOT_FOUND));

        if (magazine.getMagazineStatus() != MagazineStatus.DRAFT || magazine.getDeletedStatus() != DeletedStatus.DISPLAYED) {
            throw new CustomException(ErrorCode.MAGAZINE_NOT_FOUND);
        }

        List<String> detailImageUrls = magazineFileService.getDetailImageUrls(magazineId);

        return MagazineDraftDetailResponseDto.builder()
            .id(magazine.getId())
            .title(magazine.getTitle())
            .content(magazine.getContent())
            .thumnailUrl(magazine.getThumbnailUrl())
            .detailImageUrls(detailImageUrls)
            .regionName(magazine.getRegionCategory().getRegionName())
            .aptitudeName(magazine.getAptitudeCategory().getAptitudeName())
            .adminEmail(magazine.getAdmin().getEmail())
            .createdAt(magazine.getCreatedAt())
            .build();
    }

}
