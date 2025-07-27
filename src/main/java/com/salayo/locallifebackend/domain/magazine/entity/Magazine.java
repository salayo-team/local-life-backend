package com.salayo.locallifebackend.domain.magazine.entity;

import com.salayo.locallifebackend.domain.category.entity.AptitudeCategory;
import com.salayo.locallifebackend.domain.category.entity.RegionCategory;
import com.salayo.locallifebackend.domain.magazine.enums.MagazineStatus;
import com.salayo.locallifebackend.domain.member.entity.Member;
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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "magazine")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Magazine extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(nullable = false, length = 500)
    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MagazineStatus magazineStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeletedStatus deletedStatus;

    @Column(nullable = false)
    private Long views;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_category_id")
    private RegionCategory regionCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aptitude_category_id")
    private AptitudeCategory aptitudeCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private Member admin;

    @Builder
    public Magazine(String title, String content, String thumbnailUrl, MagazineStatus magazineStatus, DeletedStatus deletedStatus,
        Long views, RegionCategory regionCategory, AptitudeCategory aptitudeCategory, Member admin) {
        this.title = title;
        this.content = content;
        this.thumbnailUrl = thumbnailUrl;
        this.magazineStatus = magazineStatus;
        this.deletedStatus = deletedStatus;
        this.views = views;
        this.regionCategory = regionCategory;
        this.aptitudeCategory = aptitudeCategory;
        this.admin = admin;
    }

    public void updateStatus(MagazineStatus magazineStatus) {
        this.magazineStatus = magazineStatus;
    }

    public void increaseViews() {
        this.views++;
    }

    public void updateTitleAndContent(String title, String content) {
        this.title = title;
        this.content = content;
    }

}
