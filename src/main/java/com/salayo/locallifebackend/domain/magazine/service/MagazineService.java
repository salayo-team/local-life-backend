package com.salayo.locallifebackend.domain.magazine.service;

import com.salayo.locallifebackend.domain.category.entity.AptitudeCategory;
import com.salayo.locallifebackend.domain.category.entity.RegionCategory;
import com.salayo.locallifebackend.domain.category.repository.AptitudeCategoryRepository;
import com.salayo.locallifebackend.domain.category.repository.RegionCategoryRepository;
import com.salayo.locallifebackend.domain.email.service.EmailService;
import com.salayo.locallifebackend.domain.localcreator.entity.LocalCreator;
import com.salayo.locallifebackend.domain.localcreator.repository.LocalCreatorRepository;
import com.salayo.locallifebackend.domain.magazine.dto.MagazineCreateRequestDto;
import com.salayo.locallifebackend.domain.magazine.dto.MagazineDraftDetailResponseDto;
import com.salayo.locallifebackend.domain.magazine.dto.MagazineDraftListResponseDto;
import com.salayo.locallifebackend.domain.magazine.entity.Magazine;
import com.salayo.locallifebackend.domain.magazine.entity.MagazinePreviewToken;
import com.salayo.locallifebackend.domain.magazine.enums.MagazineStatus;
import com.salayo.locallifebackend.domain.magazine.repository.MagazinePreviewTokenRepository;
import com.salayo.locallifebackend.domain.magazine.repository.MagazineRepository;
import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.member.repository.MemberRepository;
import com.salayo.locallifebackend.global.dto.PaginationResponseDto;
import com.salayo.locallifebackend.global.enums.DeletedStatus;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MagazineService {

    private final MagazineRepository magazineRepository;
    private final RegionCategoryRepository regionCategoryRepository;
    private final AptitudeCategoryRepository aptitudeCategoryRepository;
    private final MemberRepository memberRepository;
    private final MagazineFileService magazineFileService;
    private final MagazinePreviewTokenRepository magazinePreviewTokenRepository;
    private final EmailService emailService;
    private final LocalCreatorRepository localCreatorRepository;

    @Value("${frontend.preview-url}")
    private String previewBaseUrl;

    public MagazineService(MagazineRepository magazineRepository, RegionCategoryRepository regionCategoryRepository,
        AptitudeCategoryRepository aptitudeCategoryRepository, MemberRepository memberRepository, MagazineFileService magazineFileService,
        MagazinePreviewTokenRepository magazinePreviewTokenRepository, EmailService emailService,
        LocalCreatorRepository localCreatorRepository) {
        this.magazineRepository = magazineRepository;
        this.regionCategoryRepository = regionCategoryRepository;
        this.aptitudeCategoryRepository = aptitudeCategoryRepository;
        this.memberRepository = memberRepository;
        this.magazineFileService = magazineFileService;
        this.magazinePreviewTokenRepository = magazinePreviewTokenRepository;
        this.emailService = emailService;
        this.localCreatorRepository = localCreatorRepository;
    }

    @Transactional
    public void createMagazine(MagazineCreateRequestDto createRequestDto, Long adminId) {
        RegionCategory region = regionCategoryRepository.findByIdOrElseThrow(createRequestDto.getRegionCategoryId());
        AptitudeCategory aptitude = aptitudeCategoryRepository.findByIdOrElseThrow(createRequestDto.getAptitudeCategoryId());
        Member admin = memberRepository.findByIdOrElseThrow(adminId);

        LocalCreator localCreator = localCreatorRepository.findByIdOrThrow(createRequestDto.getLocalCreatorId());

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
            .localCreator(localCreator)
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

    public PaginationResponseDto<MagazineDraftListResponseDto> getDraftMagazines(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Magazine> magazinePage = magazineRepository.findByMagazineStatusAndDeletedStatus(
            MagazineStatus.DRAFT,
            DeletedStatus.DISPLAYED,
            pageable
        );

        Page<MagazineDraftListResponseDto> dtoPage = magazinePage.map(m -> MagazineDraftListResponseDto.builder()
            .id(m.getId())
            .title(m.getTitle())
            .thumbnailUrl(m.getThumbnailUrl())
            .regionName(m.getRegionCategory().getRegionName())
            .aptitudeName(m.getAptitudeCategory().getAptitudeName())
            .createdAt(m.getCreatedAt())
            .build());

        return PaginationResponseDto.of(dtoPage);
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

    @Transactional
    public void sendPreviewLink(Long magazineId) {
        Magazine magazine = magazineRepository.findById(magazineId)
            .orElseThrow(() -> new CustomException(ErrorCode.MAGAZINE_NOT_FOUND));

        String email = magazine.getLocalCreator().getMember().getEmail();
        String businessName = magazine.getLocalCreator().getBusinessName();

        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(14);

        MagazinePreviewToken previewToken = new MagazinePreviewToken(magazine, email, token, expiresAt);
        magazinePreviewTokenRepository.save(previewToken);

        String previewUrl = previewBaseUrl + token;

        emailService.sendPreviewLinkEmail(email, businessName, previewUrl);
    }

}
