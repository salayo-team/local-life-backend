package com.salayo.locallifebackend.global.init;

import com.salayo.locallifebackend.domain.review.entity.ReviewTag;
import com.salayo.locallifebackend.domain.review.repository.ReviewTagRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Configuration
@Profile("local")
public class ReviewTagDataInit {

	@Bean
	public CommandLineRunner initReviewTagData(
		ReviewTagRepository reviewTagRepository
	) {
		return args -> {
			String[] tagNames = {
				"현지 전문가가 진행해요",
				"맞춤 수업을 잘해줘요",
				"친절했어요",
				"편안했어요",
				"쾌적했어요",
				"알찬 시간구성이었어요",
				"가성비가 좋아요",
				"재방문 의사 있어요",
				"초보자도 쉽게 따라해요",
				"특별한 경험이었어요"
			};

			for (int i = 0; i < tagNames.length; i++) {
				String tagName = tagNames[i];
				if (!reviewTagRepository.existsByTagName(tagName)) {
					reviewTagRepository.save(
						ReviewTag.builder()
							.tagName(tagName)
							.displayOrder(i + 1)
							.isActive(true)
							.build()
					);
				}
			}
		};
	}
}
