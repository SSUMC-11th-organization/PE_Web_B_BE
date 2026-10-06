// src/main/java/.../controller/RentalController.java
package com.umc.study.controller;

import com.umc.study.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/rentals") // 이 컨트롤러의 기본 주소는 /rentals 입니다.
@RequiredArgsConstructor
public class RentalController {

    private final BookService bookService;

    // POST http://localhost:8080/rentals
    // Request Body로 userId와 bookId를 JSON 형태로 전달받습니다.
    /*
    @PostMapping
    public String createRental(@RequestBody Map<String, Object> body) {
        bookService.createRental(body);
        return "도서 대여 기록이 정상적으로 생성되었습니다!";
    }
    */
}