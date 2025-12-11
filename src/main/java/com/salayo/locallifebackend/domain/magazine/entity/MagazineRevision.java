package com.salayo.locallifebackend.domain.magazine.entity;

import com.salayo.locallifebackend.domain.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "magazine_revisions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class MagazineRevision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "magazine_id", nullable = false)
    private Magazine magazine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member admin;

    @Column(nullable = false,columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private int revisionNumber;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public MagazineRevision(Magazine magazine, String content, int revisionNumber) {
        this.magazine = magazine;
        this.content = content;
        this.revisionNumber = revisionNumber;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public MagazineRevision(Magazine magazine, Member admin, String content, int revisionNumber) {
        this.magazine = magazine;
        this.admin = admin;
        this.content = content;
        this.revisionNumber = revisionNumber;
    }

}
