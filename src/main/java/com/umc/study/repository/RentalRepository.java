package com.umc.study.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class RentalRepository {

    private final JdbcTemplate jdbcTemplate;

    public void save(Map<String, Object> body) {
        // rented_at은 지금, due_at은 7일 뒤로 MySQL 함수가 채움. returned_at은 생략 → NULL(미반납)
        String sql = "INSERT INTO rental (user_id, book_id, rented_at, due_at) "
                + "VALUES (?, ?, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY))";

        jdbcTemplate.update(
                sql,
                body.get("userId"),    // 첫 번째 ?
                body.get("bookId")     // 두 번째 ?
        );
    }
}
