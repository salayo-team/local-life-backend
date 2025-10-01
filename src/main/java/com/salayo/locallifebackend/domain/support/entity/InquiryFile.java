package com.salayo.locallifebackend.domain.support.entity;

import com.salayo.locallifebackend.domain.file.enums.FilePurpose;
import com.salayo.locallifebackend.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "inquiry_file")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryFile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileUrl;

    @Enumerated(EnumType.STRING)
    private FilePurpose purpose;

    @ManyToOne(fetch = FetchType.LAZY)
    private Inquiry inquiry;

    public InquiryFile(String fileUrl, FilePurpose purpose, Inquiry inquiry) {
        this.fileUrl = fileUrl;
        this.purpose = purpose;
        this.inquiry = inquiry;
    }

}
