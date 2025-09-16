package com.salayo.locallifebackend.domain.magazine.feedback.entity;

import com.salayo.locallifebackend.domain.localcreator.entity.LocalCreator;
import com.salayo.locallifebackend.domain.magazine.entity.Magazine;
import com.salayo.locallifebackend.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "magazine_feedback")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MagazineFeedback extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "magazine_id", nullable = false)
    private Magazine magazine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "local_creator_id", nullable = false)
    private LocalCreator localCreator;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private int revisionCount;

    @Column(nullable = false)
    private boolean reflected;

    @Builder
    public MagazineFeedback(Magazine magazine, LocalCreator localCreator, String content, int revisionCount) {
        this.magazine = magazine;
        this.localCreator = localCreator;
        this.content = content;
        this.revisionCount = revisionCount;
        this.reflected = false;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void markAsReflected() {
        this.reflected = true;
    }

}
