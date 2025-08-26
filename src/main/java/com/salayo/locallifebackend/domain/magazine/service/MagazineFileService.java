package com.salayo.locallifebackend.domain.magazine.service;

import com.salayo.locallifebackend.domain.file.entity.File;
import com.salayo.locallifebackend.domain.file.entity.FileMapping;
import com.salayo.locallifebackend.domain.file.enums.FileCategory;
import com.salayo.locallifebackend.domain.file.enums.FilePurpose;
import com.salayo.locallifebackend.domain.file.repository.FileMappingRepository;
import com.salayo.locallifebackend.domain.file.repository.FileRepository;
import com.salayo.locallifebackend.domain.file.util.S3Uploader;
import com.salayo.locallifebackend.domain.magazine.dto.MagazineFileUploadResponseDto;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MagazineFileService {

    private final FileRepository fileRepository;
    private final FileMappingRepository fileMappingRepository;
    private final S3Uploader s3Uploader;

    public MagazineFileService(FileRepository fileRepository, FileMappingRepository fileMappingRepository, S3Uploader s3Uploader) {
        this.fileRepository = fileRepository;
        this.fileMappingRepository = fileMappingRepository;
        this.s3Uploader = s3Uploader;
    }

    public List<MagazineFileUploadResponseDto> uploadFiles(List<MultipartFile> files, FilePurpose filePurpose) {
        if (filePurpose == FilePurpose.THUMBNAIL && files.size() > 1) {
            throw new CustomException(ErrorCode.THUMBNAIL_LIMIT_EXCEEDED);
        }

        List<MagazineFileUploadResponseDto> fileUpload = new ArrayList<>();

        for (MultipartFile file : files) {
            String storeUrl = s3Uploader.upload(file, "magazine");

            File savedFile = fileRepository.save(
                File.builder()
                    .originalName(file.getOriginalFilename())
                    .storedFileName(storeUrl)
                    .build()
            );

            fileMappingRepository.save(FileMapping.builder()
                .file(savedFile)
                .fileCategory(FileCategory.MAGAZINE)
                .referenceId(0L)
                .filePurpose(filePurpose)
                .build());

            fileUpload.add(MagazineFileUploadResponseDto.builder().fileUrl(storeUrl).build());
        }
        return fileUpload;
    }

    public List<String> getDetailImageUrls(Long magazineId) {
        List<FileMapping> mappings = fileMappingRepository.findAllByFileCategoryAndReferenceIdAndFilePurpose(
            FileCategory.MAGAZINE, magazineId, FilePurpose.DETAIL_IMAGE
        );

        return mappings.stream()
            .map(mapping -> mapping.getFile().getStoredFileName())
            .collect(Collectors.toList());
    }

    @Transactional
    public void updateFileReferenceIdByUrl(String url, Long magazineId) {
        int updated = fileMappingRepository.updateReferenceIdByStoredFileNameAndZeroReference(url, magazineId);
    }

}
