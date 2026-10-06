package com.umc.study.controller;

import com.umc.study.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController              // 결과를 JSON으로 응답하는 컨트롤러
@RequestMapping("/books")    // 이 클래스의 기본 주소
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping              // GET /books
    public List<Map<String, Object>> getBooks() {
        return bookService.getAllBooks();   // List<Map> → JSON 배열로 자동 변환
    }

    @GetMapping("/category/{categoryId}")                    // GET /books/category/1
    public List<Map<String, Object>> getBooksByCategory(
            @PathVariable("categoryId") Long categoryId) {   // 주소의 {categoryId} 값
        return bookService.getBooksByCategory(categoryId);
    }

    @PostMapping                                         // POST /books
    public String createBook(@RequestBody Map<String, Object> body) {   // JSON → Map
        bookService.createBook(body);
        return "도서 등록이 완료되었습니다!";
    }
}
