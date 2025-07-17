package com.salayo.locallifebackend.domain.localcreator.entity;

import com.salayo.locallifebackend.domain.localcreator.enums.CreatorStatus;
import com.salayo.locallifebackend.domain.member.entity.Member;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "local_creator")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LocalCreator extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String businessName;

    @Column(nullable = false)
    private String businessAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CreatorStatus creatorStatus;

    private String rejectedReason;

    @Builder
    public LocalCreator(Member member, String businessName, String businessAddress) {
        this.member = member;
        this.businessName = businessName;
        this.businessAddress = businessAddress;
        this.creatorStatus = CreatorStatus.PENDING;
    }

    public boolean isApproved() {
        return this.creatorStatus == CreatorStatus.APPROVED;
    }

}
