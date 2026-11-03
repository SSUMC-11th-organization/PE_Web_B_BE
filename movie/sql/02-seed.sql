USE umc_movie;
-- 반복 실행해도 기존 회원/평점을 덮어쓰지 않습니다. 아래 비밀번호는 로그인 불가능한 실습용 문자열입니다.
INSERT INTO member (email, password, nickname)
SELECT 'noco@umc.com', '$2a$10$dummyEncodedPasswordValue', '수현'
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'noco@umc.com' OR nickname = '수현');
INSERT INTO member (email, password, nickname)
SELECT 'minseo@umc.com', '$2a$10$dummyEncodedPasswordValue', '민서'
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'minseo@umc.com' OR nickname = '민서');
INSERT INTO rating (member_id, movie_id, score, comment)
SELECT member_id, 550, 5, '최고의 영화!' FROM member m
WHERE email = 'noco@umc.com' AND NOT EXISTS (SELECT 1 FROM rating r WHERE r.member_id = m.member_id AND r.movie_id = 550);
INSERT INTO rating (member_id, movie_id, score, comment)
SELECT member_id, 155, 4, '다시 봐도 명작' FROM member m
WHERE email = 'noco@umc.com' AND NOT EXISTS (SELECT 1 FROM rating r WHERE r.member_id = m.member_id AND r.movie_id = 155);
INSERT INTO rating (member_id, movie_id, score, comment)
SELECT member_id, 550, 3, '호불호가 갈릴 것 같아요' FROM member m
WHERE email = 'minseo@umc.com' AND NOT EXISTS (SELECT 1 FROM rating r WHERE r.member_id = m.member_id AND r.movie_id = 550);
SELECT member_id, email, nickname FROM member ORDER BY member_id;
SELECT rating_id, member_id, movie_id, score, comment FROM rating ORDER BY rating_id;
