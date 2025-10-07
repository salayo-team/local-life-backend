package com.salayo.locallifebackend.domain.support.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "inquiry_template")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String category;

    public InquiryTemplate(String title, String content, String category) {
        this.title = title;
        this.content = content;
        this.category = category;
    }

}
