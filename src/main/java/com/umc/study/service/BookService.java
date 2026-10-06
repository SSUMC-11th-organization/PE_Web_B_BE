// src/main/java/.../service/BookService.java
package com.umc.study.service;

import com.umc.study.domain.*;
import com.umc.study.dto.BookResponse;
import com.umc.study.dto.CreateBookRequest;
import com.umc.study.repository.BookRepository;
import com.umc.study.repository.CategoryRepository;
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
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<BookResponse> getBooks() {
        return bookRepository.findAllByOrderByBookIdDesc().stream()
                .map(BookResponse::from).toList();
    }

    @Transactional
    public BookResponse createBook(CreateBookRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new
                        IllegalArgumentException("존재하지 않는 카테고리입니다."));
        Book book = bookRepository.save(
                new Book(category, request.title(), request.description())
        );
        return BookResponse.from(book);
    }

    /*
    public List<Map<String, Object>> getAllBooks() {
        // 지금은 별도 가공 없이 창고지기가 가져온 도서 목록을 그대로 반환합니다.
        return bookRepository.findAll();
    }



    public List<Map<String, Object>> getBooksByCategoryId(Long categoryId) {
        return bookRepository.findByCategoryId(categoryId);
    }


    // BookService.java에 추가
    public void createBook(Map<String, Object> body){
        bookRepository.save(body);
    }
     */
    /*
    public void createRental(Map<String, Object> body) {
        bookRepository.saveRental(body);
    }

     */
}
