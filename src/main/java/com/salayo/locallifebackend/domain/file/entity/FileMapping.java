package com.salayo.locallifebackend.domain.file.entity;

import com.salayo.locallifebackend.domain.file.enums.FileCategory;
import com.salayo.locallifebackend.domain.file.enums.FilePurpose;
import com.salayo.locallifebackend.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "file_mapping")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileMapping extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private File file;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileCategory fileCategory;

    @Column(nullable = false)
    private Long referenceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FilePurpose filePurpose;

    @Builder
    public FileMapping(File file, FileCategory fileCategory, Long referenceId, FilePurpose filePurpose) {
        this.file = file;
        this.fileCategory = fileCategory;
        this.referenceId = referenceId;
        this.filePurpose = filePurpose;
    }
}
