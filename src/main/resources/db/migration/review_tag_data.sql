-- 리뷰 태그 초기 데이터 (태그 기반 리뷰 시스템 "이런 점이 좋았어요")
INSERT INTO review_tag (name, display_order, is_active, created_at, modified_at) VALUES
('현지 전문가가 진행해요', 1, true, NOW(), NOW()),
('맞춤 수업을 잘해줘요', 2, true, NOW(), NOW()),
('친절했어요', 3, true, NOW(), NOW()),
('편안했어요', 4, true, NOW(), NOW()),
('쾌적했어요', 5, true, NOW(), NOW()),
('알찬 시간구성이었어요', 6, true, NOW(), NOW()),
('가성비가 좋아요', 7, true, NOW(), NOW()),
('재방문 의사 있어요', 8, true, NOW(), NOW()),
('초보자도 쉽게 따라해요', 9, true, NOW(), NOW()),
('특별한 경험이었어요', 10, true, NOW(), NOW());
