package com.salayo.locallifebackend.domain.support.entity;

import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.support.enums.InquiryStatus;
import com.salayo.locallifebackend.domain.support.enums.InquiryType;
import com.salayo.locallifebackend.global.entity.BaseEntity;
import com.salayo.locallifebackend.global.enums.DeletedStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "inquiry")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inquiry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private InquiryType type;

    @Enumerated(EnumType.STRING)
    private InquiryStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member assignee;

    @Enumerated(EnumType.STRING)
    private DeletedStatus deletedStatus = DeletedStatus.DISPLAYED;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public Inquiry(String title, String content, InquiryType type, Member member) {
        this.title = title;
        this.content = content;
        this.type = type;
        this.status = InquiryStatus.WAITING;
        this.member = member;
    }

    public void assignTo(Member admin) {
        this.assignee = admin;
        this.status = InquiryStatus.IN_PROGRESS;
    }

    public void complete() {
        this.status = InquiryStatus.COMPLETED;
    }

    public void cancel() {
        this.status = InquiryStatus.CANCELED;
    }

    public void softDelete() {
        this.deletedStatus = DeletedStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }
}
