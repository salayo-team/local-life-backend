package com.salayo.locallifebackend.domain.localcreator.service;

import com.salayo.locallifebackend.domain.file.entity.File;
import com.salayo.locallifebackend.domain.file.entity.FileMapping;
import com.salayo.locallifebackend.domain.file.enums.FileCategory;
import com.salayo.locallifebackend.domain.file.repository.FileMappingRepository;
import com.salayo.locallifebackend.domain.file.util.S3Uploader;
import com.salayo.locallifebackend.domain.localcreator.dto.LocalCreatorDetailResponseDto;
import com.salayo.locallifebackend.domain.localcreator.entity.LocalCreator;
import com.salayo.locallifebackend.domain.localcreator.repository.LocalCreatorRepository;
import com.salayo.locallifebackend.domain.member.entity.Member;
import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class LocalCreatorService {

    private final LocalCreatorRepository localCreatorRepository;
    private final FileMappingRepository fileMappingRepository;
    private final S3Uploader s3Uploader;

    public LocalCreatorService(LocalCreatorRepository localCreatorRepository, FileMappingRepository fileMappingRepository,
        S3Uploader s3Uploader) {
        this.localCreatorRepository = localCreatorRepository;
        this.fileMappingRepository = fileMappingRepository;
        this.s3Uploader = s3Uploader;
    }

    public LocalCreatorDetailResponseDto getLocalCreatorDetail(Long localcreatorId) {
        LocalCreator localCreator = localCreatorRepository.findByIdOrThrow(localcreatorId);

        Member member = localCreator.getMember();

        List<FileMapping> fileMappings = fileMappingRepository
            .findAllByReferenceIdAndFileCategory(member.getId(), FileCategory.LOCAL_CREATOR);

        List<LocalCreatorDetailResponseDto.ProofFileDto> proofFiles = fileMappings.stream()
            .map(fileMapping -> {
                File file = fileMapping.getFile();
                String fileType = getFileType(file.getOriginalName());
                String key = extractKey(file.getStoredFileName());
                String url = s3Uploader.getPresignedUrl(key, Duration.ofMinutes(30));

                return LocalCreatorDetailResponseDto.ProofFileDto.builder()
                    .fileName(file.getOriginalName())
                    .fileType(fileType)
                    .url(url)
                    .filePurpose(fileMapping.getFilePurpose())
                    .fileCategory(fileMapping.getFileCategory())
                    .build();
            })
            .collect(Collectors.toList());

        return LocalCreatorDetailResponseDto.builder()
            .email(member.getEmail())
            .businessName(localCreator.getBusinessName())
            .businessAddress(localCreator.getBusinessAddress())
            .phoneNumber(member.getPhoneNumber())
            .birth(member.getBirth())
            .proofFiles(proofFiles.isEmpty() ? Collections.emptyList() : proofFiles)
            .build();
    }

    private String extractKey(String storedFileName) {
        if (storedFileName == null) return "";
        if (storedFileName.startsWith("http")) {

            try {
                URI uri = URI.create(storedFileName);

                return uri.getPath().startsWith("/") ? uri.getPath().substring(1) : uri.getPath();
            } catch (Exception e) {
                return storedFileName;
            }
        }
            return storedFileName;
    }


    private String getFileType(String fileName) {
        if (fileName == null) return "UNKNOWN";
        String ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        if (List.of("jpg", "jpeg", "png", "gif", "bmp", "webp").contains(ext)) return "IMAGE";
        if ("pdf".equals(ext)) return "PDF";
        return "UNKNOWN";
    }

}
