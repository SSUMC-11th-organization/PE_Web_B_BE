// src/main/java/.../service/BookService.java
package com.umc.study.service;

import com.umc.study.domain.Book;
import com.umc.study.domain.Category;
import com.umc.study.dto.BookResponse;
import com.umc.study.dto.CreateBookRequest;
import com.umc.study.repository.BookJdbcRepository;
import com.umc.study.repository.BookRepository;
import com.umc.study.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookService {

    // Repository를 생성자 주입으로 데려옴
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final BookJdbcRepository bookJdbcRepository;

    @Transactional(readOnly = true)
    public List<BookResponse> getBooks() {
        // 엔티티 목록을 응답 DTO로 변환
        return bookRepository.findAllByOrderByBookIdDesc().stream()
                .map(BookResponse::from)
                .toList();
    }

    @Transactional
    public BookResponse createBook(CreateBookRequest request) {
        // 존재하지 않는 카테고리면 저장하지 않고 예외
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));

        Book book = new Book(category, request.title(), request.description());
        return BookResponse.from(bookRepository.save(book));
    }

    public List<Map<String, Object>> getBooksByCategory(Long categoryId) {
        return bookJdbcRepository.findByCategoryID(categoryId);
    }
}
