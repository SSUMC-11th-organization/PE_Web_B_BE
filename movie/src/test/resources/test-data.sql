DELETE FROM rating;
DELETE FROM member;
INSERT INTO member (member_id, email, password, nickname, created_at, updated_at) VALUES
(1, 'noco@umc.com', 'dummy-not-a-real-password', '수현', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'minseo@umc.com', 'dummy-not-a-real-password', '민서', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'empty@umc.com', 'dummy-not-a-real-password', '평점없음', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO rating (rating_id, member_id, movie_id, score, comment, created_at, updated_at) VALUES
(1, 1, 550, 5, '최고의 영화!', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 155, 4, '다시 봐도 명작', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 2, 550, 3, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
