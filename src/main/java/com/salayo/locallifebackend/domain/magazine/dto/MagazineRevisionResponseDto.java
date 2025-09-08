package com.salayo.locallifebackend.domain.magazine.dto;

import com.salayo.locallifebackend.domain.magazine.entity.MagazineRevision;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class MagazineRevisionResponseDto {

    private final Long id;
    private final String content;
    private final int revisionNumber;
    private final LocalDateTime createdAt;

    public MagazineRevisionResponseDto(MagazineRevision revision) {
        this.id = revision.getId();
        this.content = revision.getContent();
        this.revisionNumber = revision.getRevisionNumber();
        this.createdAt = revision.getCreatedAt();
    }

}
