package com.salayo.locallifebackend.domain.support.entity;

import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "inquiry_answer")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryAnswer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    private Inquiry inquiry;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member admin;

    public InquiryAnswer(String content, Inquiry inquiry, Member admin) {
        this.content = content;
        this.inquiry = inquiry;
        this.admin = admin;
    }

}
