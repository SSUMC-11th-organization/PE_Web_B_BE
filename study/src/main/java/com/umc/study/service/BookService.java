// src/main/java/.../service/BookService.java
package com.umc.study.service;

import com.umc.study.dto.BookResponse;
import com.umc.study.repository.BookJdbcRepository;
import com.umc.study.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service // 비즈니스 로직을 수행하는 메인 셰프 계층
@RequiredArgsConstructor
public class BookService {

    // 창고지기(Repository)를 생성자 주입으로 데려옵니다.
    private final BookRepository bookRepository;
    private final BookJdbcRepository bookJdbcRepository;

    @Transactional(readOnly = true)
    public List<BookResponse> getBooks() {
        // 엔티티 목록을 응답 DTO로 변환합니다.
        return bookRepository.findAllByOrderByBookIdDesc().stream()
                .map(BookResponse::from)
                .toList();
    }

    public void createBook(Map<String, Object> body){
        bookJdbcRepository.save(body);
    }

    public List<Map<String, Object>> getBooksByCategory(Long categoryId) {
        return bookJdbcRepository.findByCategoryID(categoryId);
    }
}
