package com.salayo.locallifebackend.global.init;

import com.salayo.locallifebackend.domain.category.entity.AptitudeCategory;
import com.salayo.locallifebackend.domain.category.entity.RegionCategory;
import com.salayo.locallifebackend.domain.category.repository.AptitudeCategoryRepository;
import com.salayo.locallifebackend.domain.category.repository.RegionCategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local")
public class CategoryDataInit {

    @Bean
    public CommandLineRunner initCategoryData(
        RegionCategoryRepository regionCategoryRepository,
        AptitudeCategoryRepository aptitudeCategoryRepository
    ) {
        return args -> {
            String[] regionNames = {
                "서울", "부산", "대구", "인천", "광주", "대전", "울산", "세종",
                "경기도", "강원도", "충청북도", "충청남도", "전라북도", "전라남도", "경상북도", "경상남도", "제주도"
            };

            for (String name : regionNames) {
                if (!regionCategoryRepository.existsByRegionName(name)) {
                    regionCategoryRepository.save(new RegionCategory(name));
                }
            }

            String[][] aptitudes = {
                {"자연친화", "NATURE"},
                {"역사문화", "HISTORY_CULTURE"},
                {"예술창작", "ART"},
                {"커뮤니티", "COMMUNITY"},
                {"디지털기술", "TECH"}
            };

            for (String[] apt : aptitudes) {
                if (!aptitudeCategoryRepository.existsByAptitudeCode(apt[1])) {
                    aptitudeCategoryRepository.save(new AptitudeCategory(apt[0], apt[1]));
                }
            }
        };
    }
}
