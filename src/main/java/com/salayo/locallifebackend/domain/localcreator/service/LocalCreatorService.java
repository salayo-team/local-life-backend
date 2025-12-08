package com.salayo.locallifebackend.domain.localcreator.service;

import com.salayo.locallifebackend.domain.file.entity.File;
import com.salayo.locallifebackend.domain.file.entity.FileMapping;
import com.salayo.locallifebackend.domain.file.enums.FileCategory;
import com.salayo.locallifebackend.domain.file.enums.FilePurpose;
import com.salayo.locallifebackend.domain.file.repository.FileMappingRepository;
import com.salayo.locallifebackend.domain.file.repository.FileRepository;
import com.salayo.locallifebackend.domain.file.util.S3Uploader;
import com.salayo.locallifebackend.domain.localcreator.dto.LocalCreatorDetailResponseDto;
import com.salayo.locallifebackend.domain.localcreator.dto.LocalCreatorUpdateRequestDto;
import com.salayo.locallifebackend.domain.localcreator.entity.LocalCreator;
import com.salayo.locallifebackend.domain.localcreator.enums.CreatorStatus;
import com.salayo.locallifebackend.domain.localcreator.repository.LocalCreatorRepository;
import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalCreatorService {

    private final LocalCreatorRepository localCreatorRepository;
    private final FileMappingRepository fileMappingRepository;
    private final S3Uploader s3Uploader;
    private final FileRepository fileRepository;

    public LocalCreatorService(LocalCreatorRepository localCreatorRepository, FileMappingRepository fileMappingRepository,
        S3Uploader s3Uploader, FileRepository fileRepository) {
        this.localCreatorRepository = localCreatorRepository;
        this.fileMappingRepository = fileMappingRepository;
        this.s3Uploader = s3Uploader;
        this.fileRepository = fileRepository;
    }

    public LocalCreatorDetailResponseDto getLocalCreatorDetail(Long localcreatorId) {
        LocalCreator localCreator = localCreatorRepository.findByIdOrThrow(localcreatorId);

        return buildDetailResponse(localCreator);
    }

    public LocalCreatorDetailResponseDto getMyCreatorInfo(Long memberId) {
        LocalCreator localCreator = localCreatorRepository.findByMemberIdOrThrow(memberId);

        return buildDetailResponse(localCreator);
    }

    private LocalCreatorDetailResponseDto buildDetailResponse(LocalCreator localCreator) {
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
        if (storedFileName == null) {
            return "";
        }
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
        if (fileName == null) {
            return "UNKNOWN";
        }
        String ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        if (List.of("jpg", "jpeg", "png", "gif", "bmp", "webp").contains(ext)) {
            return "IMAGE";
        }
        if ("pdf".equals(ext)) {
            return "PDF";
        }
        return "UNKNOWN";
    }

    @Transactional
    public LocalCreatorDetailResponseDto updateMyCreatorInfo(Long memberId, LocalCreatorUpdateRequestDto localCreatorUpdateRequestDto,
        List<MultipartFile> files, List<FilePurpose> filePurposes) {
        LocalCreator localCreator = localCreatorRepository.findByMemberIdOrThrow(memberId);
        Member member = localCreator.getMember();

        if (localCreator.getCreatorStatus() == CreatorStatus.PENDING) {
            throw new CustomException(ErrorCode.LOCAL_CREATOR_NOT_APPROVED);
        }

        localCreator.requestReapprove(localCreatorUpdateRequestDto.getBusinessName(), localCreatorUpdateRequestDto.getBusinessAddress());

        fileMappingRepository.deleteAllByReferenceIdAndFileCategory(member.getId(), FileCategory.LOCAL_CREATOR);

        for (int i = 0; i < filePurposes.size(); i++) {
            FilePurpose filePurpose = filePurposes.get(i);
            MultipartFile multipartFile = files.get(i);

            if (filePurpose == FilePurpose.BANK_ACCOUNT_COPY && (multipartFile == null || multipartFile.isEmpty())) {
                throw new CustomException(ErrorCode.MISSING_REQUIRED_FILE);
            }

            if (multipartFile == null || multipartFile.isEmpty()) {
                continue;
            }

            String storeUrl = s3Uploader.upload(multipartFile, "local-creator");

            File savedFile = fileRepository.save(
                File.builder()
                    .originalName(multipartFile.getOriginalFilename())
                    .storedFileName(storeUrl)
                    .build()
            );

            fileMappingRepository.save(
                FileMapping.builder()
                    .file(savedFile)
                    .fileCategory(FileCategory.LOCAL_CREATOR)
                    .referenceId(member.getId())
                    .filePurpose(filePurpose)
                    .build()
            );
        }

        return buildDetailResponse(localCreator);
    }

}
