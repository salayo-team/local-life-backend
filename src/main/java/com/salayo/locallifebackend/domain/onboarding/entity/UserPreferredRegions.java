package com.salayo.locallifebackend.domain.onboarding.entity;

import com.salayo.locallifebackend.domain.member.entity.Member;
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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 선호 지역 엔티티
 * 사용자가 선택한 지역 특징(RegionType)에 따라 자동으로 매핑되는 지역들을 저장
 */
@Entity
@Table(
    name = "user_preferred_regions",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"member_id", "region_name"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPreferredRegions extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "preferred_region_id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    
    @Column(name = "region_name", nullable = false, length = 20)
    private String regionName; // 지역명 (ex: "서울", "부산", "강원" 등)
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true; // 활성화 여부
    
    @Builder
    public UserPreferredRegions(Member member, String regionName, boolean isActive) {
        this.member = member;
        this.regionName = regionName;
        this.isActive = isActive;
    }
    
    /**
     * 선호 지역 활성화/비활성화
     */
    public void updateActiveStatus(boolean isActive) {
        this.isActive = isActive;
    }
}
