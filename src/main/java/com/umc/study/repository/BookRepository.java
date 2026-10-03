package com.umc.study.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

@Repository                 // 창고지기 부품으로 등록
@RequiredArgsConstructor    // final 필드를 받는 생성자 자동 생성 → 주입
public class BookRepository {

    private final JdbcTemplate jdbcTemplate;   // Spring이 만들어 둔 SQL 실행 도구

    public List<Map<String, Object>> findAll() {
        String sql = "SELECT * FROM book";
        // 결과: 행 하나 = Map 하나 (key: 컬럼명, value: 값)
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> findByCategoryId(Long categoryId) {
        String sql = "SELECT * FROM book WHERE category_id = ?";
        return jdbcTemplate.queryForList(sql, categoryId);   // ? 자리에 categoryId
    }

    public void save(Map<String, Object> body) {
        // book_id는 AUTO_INCREMENT라 생략, is_available은 true로 고정
        String sql = "INSERT INTO book (category_id, title, description, is_available) VALUES (?, ?, ?, true)";

        jdbcTemplate.update(
                sql,
                body.get("categoryId"),    // 첫 번째 ?
                body.get("title"),         // 두 번째 ?
                body.get("description")    // 세 번째 ?
        );
    }
}
