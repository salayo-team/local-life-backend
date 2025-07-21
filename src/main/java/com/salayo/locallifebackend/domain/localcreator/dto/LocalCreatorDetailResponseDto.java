package com.salayo.locallifebackend.domain.localcreator.dto;

import com.salayo.locallifebackend.domain.file.enums.FileCategory;
import com.salayo.locallifebackend.domain.file.enums.FilePurpose;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LocalCreatorDetailResponseDto {

    private String email;
    private String businessName;
    private String businessAddress;
    private String phoneNumber;
    private String birth;
    private List<ProofFileDto> proofFiles;

    @Getter
    @Builder
    public static class ProofFileDto {
        private String fileName;
        private String fileType;
        private String url;
        private FilePurpose filePurpose;
        private FileCategory fileCategory;
    }

}
