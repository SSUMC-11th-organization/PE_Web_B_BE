// src/main/java/.../repository/BookJdbcRepository.java
package com.umc.study.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class BookJdbcRepository {

    // 2단계에서 준비된 스프링의 DB 통신 도구(JdbcTemplate) 주입
    private final JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> findAll() {
        String sql = "SELECT * FROM book";

        // 쿼리를 실행하고 결과를 List<Map> 형태의 날것 데이터로
        // Map의 Key는 '컬럼명(title)', Value는 '실제 데이터(달빛 도서관)'
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> findByCategoryID(Long categoryId) {
        String sql = "SELECT * FROM book WHERE category_id = ?";
        return jdbcTemplate.queryForList(sql, categoryId);
    }
}

