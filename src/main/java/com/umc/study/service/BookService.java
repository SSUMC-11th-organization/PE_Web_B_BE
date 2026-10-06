package com.umc.study.service;

import com.umc.study.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;   // 창고지기 주입

    public List<Map<String, Object>> getAllBooks() {
        return bookRepository.findAll();           // 지금은 그대로 전달
    }

    public List<Map<String, Object>> getBooksByCategory(Long categoryId) {
        return bookRepository.findByCategoryId(categoryId);
    }

    public void createBook(Map<String, Object> body) {
        bookRepository.save(body);
    }
}
